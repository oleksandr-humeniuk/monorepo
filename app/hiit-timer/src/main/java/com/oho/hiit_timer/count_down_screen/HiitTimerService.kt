package com.oho.hiit_timer.count_down_screen

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.oho.hiit_timer.HiitActivity
import com.oho.hiit_timer.count_down_screen.NotificationHelper.Action
import com.oho.hiit_timer.data.HiitWorkoutsRepository
import com.oho.hiit_timer.data.storage.HiitRunSessionDao
import com.oho.hiit_timer.data.storage.HiitRunSessionEntity
import com.oho.hiit_timer.domain.HiitPlanner
import com.oho.hiit_timer.domain.HiitSegment
import com.oho.hiit_timer.domain.HiitWorkout
import com.oho.hiit_timer.formatSec
import com.oho.utils.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.math.max

class HiitRunService : Service(), KoinComponent {

    private val workoutsRepo: HiitWorkoutsRepository by inject()
    private val sessionDao: HiitRunSessionDao by inject()

    private val binder = LocalBinder()
    @Volatile
    private var soundPreloaded = false

    private suspend fun ensureSoundsReady() {
        if (soundPreloaded) return
        soundController.preload()     // suspends until all required samples loaded
        soundPreloaded = true
    }

    override fun onBind(intent: Intent?): IBinder = binder

    // ----------------------------
    // Public API (bound clients)
    // ----------------------------

    interface HiitRunController {
        val state: StateFlow<ViewState>
        fun send(cmd: Cmd)
    }

    inner class LocalBinder : Binder(), HiitRunController {
        override val state: StateFlow<ViewState>
            get() = this@HiitRunService.state

        override fun send(cmd: Cmd) = this@HiitRunService.send(cmd)
    }

    sealed interface Cmd {
        data class Start(val workoutId: String) : Cmd
        data object PauseResume : Cmd
        data object Next : Cmd
        data object Previous : Cmd
        data object Stop : Cmd
        data object Open : Cmd
    }

    fun send(cmd: Cmd) {
        commands.trySend(cmd)
    }

    val state: StateFlow<ViewState> get() = _state

    // ----------------------------
    // Runtime (NO DB on ticks)
    // ----------------------------

    private val svcJob = SupervisorJob()
    private val scope = CoroutineScope(svcJob + Dispatchers.Default)
    private val commands = Channel<Cmd>(capacity = Channel.BUFFERED)

    private data class RuntimeSession(
        val workoutId: String,
        val segmentIndex: Int,
        val segmentStartedAtEpochMs: Long,
        val isPaused: Boolean,
        val pausedAtEpochMs: Long?,
        val accumulatedPausedMs: Long,
        val isFinished: Boolean,
        val createdAtEpochMs: Long,
    )

    @Volatile
    private var runtime: RuntimeSession? = null

    // Planned workout
    private var workout: HiitWorkout? = null
    private var segments: List<HiitSegment> = emptyList()
    private var doneIndex: Int = 0
    private var totalPhases: Int = 0

    private var futureDurationSec: IntArray = IntArray(0)
    private var restOrdinalInExercise: IntArray = IntArray(0)
    private var restTotalByExercise: Map<String, Int> = emptyMap()

    private val soundController by lazy {
        HiitSoundController(
            context = applicationContext,
            config = HiitSoundController.Config(
                workStartRes = R.raw.boxing_bell,
                restStartRes = R.raw.whistle,
                workoutFinishedRes = R.raw.whistle,
                volume = 0.8f,
                enabled = true,
                countdown = HiitSoundController.Config.Countdown(
                    beepRes = R.raw.countdown_tick,
                    seconds = setOf(3, 2, 1),
                    volumeMultiplier = 0.6f,
                    includePrepare = false,
                )
            )
        )
    }

    // --------------------------------
    // UI state (avoid "blink" on open)
    // --------------------------------

    private val _state: MutableStateFlow<ViewState> = MutableStateFlow(
        ViewState.Idle
    )

    // ----------------------------
    // Notification tick throttling
    // ----------------------------

    private var lastNotifiedSecond: Int = -1
    private var isForegroundStarted = false

    // ----------------------------
    // Intents from notification
    // ----------------------------

    private companion object {
        const val EXTRA_CMD = "extra_cmd"
        const val CMD_PAUSE_RESUME = "pause_resume"
        const val CMD_STOP = "stop"
        const val CMD_OPEN = "open"
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannel(this)

        scope.launch {
            restoreIfAny()         // restore -> set runtime + render + foreground
            launch { commandLoop() }
        }
    }

    override fun onDestroy() {
        soundController.release()
        svcJob.cancel()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.getStringExtra(EXTRA_CMD)) {
            CMD_PAUSE_RESUME -> send(Cmd.PauseResume)
            CMD_STOP -> send(Cmd.Stop)
            CMD_OPEN -> send(Cmd.Open)
        }
        return START_STICKY
    }

    // ---------------------------------------
    // Restore / Start / Stop session clearing
    // ---------------------------------------

    /**
     * When to clear session:
     * - On STOP (user stops workout) => clear immediately.
     * - On FINISH (Done reached) => keep for a short time OR clear immediately (choose).
     *   To avoid "previous session blink" when entering run screen:
     *   - Option A: clear immediately on finish.
     *   - Option B: keep finished session but UI must treat it as Done with isPaused=true.
     *
     * We choose A: clear on finish after showing "Done" state + notification update.
     */
    private suspend fun restoreIfAny() {
        val s = sessionDao.get() ?: return
        if (s.isFinished) {
            // Clear old finished runs so they never "blink"
            sessionDao.clear()
            return
        }

        val w = workoutsRepo.getWorkout(s.workoutId) ?: run {
            sessionDao.clear()
            return
        }

        applyWorkout(w)
        ensureSoundsReady()

        runtime = s.toRuntime()
        renderFromRuntime(requireNotNull(runtime), nowEpochMs = System.currentTimeMillis())

        ensureForeground() // must happen after render so notif text is correct
    }

    // ---------------------------------------
    // Command loop
    // ---------------------------------------

    private suspend fun commandLoop() {
        for (cmd in commands) {
            when (cmd) {
                is Cmd.Start -> handleStart(cmd.workoutId)
                Cmd.PauseResume -> handlePauseResume()
                Cmd.Next -> handleNext(manual = true)
                Cmd.Previous -> handlePrevious()
                Cmd.Stop -> handleStop()
                Cmd.Open -> openTimerUi()
            }
        }
    }

    @Volatile
    private var tickJob: Job? = null
    private suspend fun handleStart(workoutId: String) {
        val w = workoutsRepo.getWorkout(workoutId) ?: return
        applyWorkout(w)
        ensureSoundsReady()
        soundController.resetForNewRun()

        val now = System.currentTimeMillis()

        val r = RuntimeSession(
            workoutId = workoutId,
            segmentIndex = 0,
            segmentStartedAtEpochMs = now,
            isPaused = false,
            pausedAtEpochMs = null,
            accumulatedPausedMs = 0L,
            isFinished = false,
            createdAtEpochMs = now,
        )
        runtime = r
        sessionDao.upsert(r.toEntity(updatedAt = now))

        renderFromRuntime(r, nowEpochMs = now)
        ensureForeground()

        // cue
        soundController.onSegmentChanged(
            nextIndex = 0,
            next = segments[0],
            isPaused = false,
        )
        notifyIfNeeded(force = true)

        tickJob?.cancel()
        tickJob = scope.launch { tickerLoop() }
    }

    private suspend fun handlePauseResume() {
        val r0 = runtime ?: return
        if (r0.isFinished) return

        val now = System.currentTimeMillis()
        val r1 = if (!r0.isPaused) {
            r0.copy(isPaused = true, pausedAtEpochMs = now)
        } else {
            val pausedFor = max(0L, now - (r0.pausedAtEpochMs ?: now))
            r0.copy(
                isPaused = false,
                pausedAtEpochMs = null,
                accumulatedPausedMs = r0.accumulatedPausedMs + pausedFor
            )
        }

        runtime = r1
        sessionDao.upsert(r1.toEntity(updatedAt = now))

        renderFromRuntime(r1, nowEpochMs = now)
        notifyIfNeeded(force = true)
    }

    private suspend fun handleNext(manual: Boolean) {
        val r0 = runtime ?: return
        if (r0.isFinished) return

        val next = (r0.segmentIndex + 1).coerceAtMost(doneIndex)
        if (next == r0.segmentIndex) return

        val now = System.currentTimeMillis()
        val finished = next >= doneIndex || (segments.getOrNull(next) is HiitSegment.Done)

        val r1 = r0.copy(
            segmentIndex = next,
            segmentStartedAtEpochMs = now,
            isPaused = false,
            pausedAtEpochMs = null,
            accumulatedPausedMs = 0L,
            isFinished = finished,
        )

        runtime = r1
        sessionDao.upsert(r1.toEntity(updatedAt = now))

        soundController.onSegmentChanged(
            nextIndex = next,
            next = segments[next],
            isPaused = false,
        )

        renderFromRuntime(r1, nowEpochMs = now)
        notifyIfNeeded(force = true)

        if (finished) {
            // show Done briefly in notification, then clear session to avoid next-open blink
            NotificationHelper.notify(
                ctx = this,
                title = "HIIT Timer",
                text = "Workout complete",
                ongoing = false,
                actions = listOf(Action.Open),
            )
            sessionDao.clear()
            runtime = null
            stopForeground(STOP_FOREGROUND_DETACH)
            stopSelf()
        }
    }

    private suspend fun handlePrevious() {
        val r0 = runtime ?: return
        if (r0.isFinished) return

        val prev = (r0.segmentIndex - 1).coerceAtLeast(0)
        val now = System.currentTimeMillis()

        val r1 = r0.copy(
            segmentIndex = prev,
            segmentStartedAtEpochMs = now,
            isPaused = false,
            pausedAtEpochMs = null,
            accumulatedPausedMs = 0L,
        )

        runtime = r1
        sessionDao.upsert(r1.toEntity(updatedAt = now))

        soundController.onSegmentChanged(
            nextIndex = prev,
            next = segments[prev],
            isPaused = false,
        )

        renderFromRuntime(r1, nowEpochMs = now)
        notifyIfNeeded(force = true)
    }

    private suspend fun handleStop() {
        sessionDao.clear()
        runtime = null

        _state.value = ViewState.Loaded(
            HiitRunUiState(
                phase = HiitPhase.Done,
                totalRemaining = 0,
                phaseRemaining = 0,
                nextLabel = null,
                isPaused = true,
                phaseLabel = "Done",
                setIndex = 0,
                setsTotal = 0,
                phaseIndex = 0,
                totalPhases = 0,
                restIndex = 0,
                totalRest = 0,
            )
        )

        NotificationHelper.notify(
            ctx = this,
            title = "HIIT Timer",
            text = "Stopped",
            ongoing = false,
            actions = listOf(Action.Open),
        )

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun openTimerUi() {
        // Replace HiitRunActivity with your actual entry point.
        val i = Intent(this, HiitActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        startActivity(i)
    }

    // ---------------------------------------
    // Ticker loop (RAM-only)
    // ---------------------------------------

    private suspend fun tickerLoop() {
        while (scope.isActive) {
            delay(100L)

            val r = runtime ?: continue
            if (r.isFinished) continue
            if (segments.isEmpty()) continue

            val now = System.currentTimeMillis()
            val remSec = remainingSec(r, now)

            // countdown
            val idx = r.segmentIndex.coerceIn(0, segments.lastIndex)
            soundController.onTick(
                currentIndex = idx,
                currentSegment = segments[idx],
                phaseRemainingSec = remSec,
                isPaused = r.isPaused,
            )

            if (!r.isPaused && remSec <= 0) {
                handleNext(manual = false)
            } else {
                renderFromRuntime(r, nowEpochMs = now)
                notifyIfNeeded(force = false)
            }
        }
    }

    // ---------------------------------------
    // Notification update policy
    // ---------------------------------------

    /**
     * Updating notification every 100ms is overkill.
     * Update at most once per second (or when forced).
     */
    private fun notifyIfNeeded(force: Boolean) {
        if (!isForegroundStarted) return

        val s = (_state.value as? ViewState.Loaded)?.runUiState ?: return
        val sec = s.phaseRemaining

        if (!force && sec == lastNotifiedSecond) return
        lastNotifiedSecond = sec

        val actions = buildList {
            add(Action.Open)
            add(if (s.isPaused) Action.Resume else Action.Pause)
            add(Action.Stop)
        }

        NotificationHelper.notify(
            ctx = this,
            title = "HIIT Timer",
            text = "${s.phaseLabel}: ${formatSec(s.phaseRemaining)}",
            ongoing = true,
            actions = actions,
        )
    }

    private fun ensureForeground() {
        if (isForegroundStarted) return
        val s = (_state.value as? ViewState.Loaded)?.runUiState ?: return

        val actions = listOf(Action.Open, Action.Pause, Action.Stop)

        val notif = NotificationHelper.build(
            ctx = this,
            title = "HIIT Timer",
            text = "${s.phaseLabel}: ${formatSec(s.phaseRemaining)}",
            ongoing = true,
            actions = actions,
        )
        startForeground(NotificationHelper.NOTIF_ID, notif)
        isForegroundStarted = true
        lastNotifiedSecond = -1
    }

    // ---------------------------------------
    // Planning + UI mapping (your logic, fixed)
    // ---------------------------------------

    private fun applyWorkout(w: HiitWorkout) {
        workout = w
        segments = HiitPlanner.plan(w)

        doneIndex = segments.indexOfLast { it is HiitSegment.Done }
            .takeIf { it >= 0 } ?: segments.lastIndex
        totalPhases = max(0, doneIndex)

        futureDurationSec = IntArray(segments.size) { 0 }
        var sum = 0
        for (i in (doneIndex - 1) downTo 0) {
            sum += segments[i + 1].durationSec.coerceAtLeast(0)
            futureDurationSec[i] = sum
        }

        restOrdinalInExercise = IntArray(segments.size) { 0 }
        val totals = HashMap<String, Int>()
        val cursors = HashMap<String, Int>()
        for (i in 0 until doneIndex) {
            val seg = segments[i]
            if (seg is HiitSegment.Rest) {
                val name = seg.exerciseName
                totals[name] = (totals[name] ?: 0) + 1
                val nextOrdinal = (cursors[name] ?: 0) + 1
                cursors[name] = nextOrdinal
                restOrdinalInExercise[i] = nextOrdinal
            }
        }
        restTotalByExercise = totals
    }

    private fun renderFromRuntime(r: RuntimeSession, nowEpochMs: Long) {
        val idx = r.segmentIndex.coerceIn(0, segments.lastIndex)
        val seg = segments[idx]
        val isDone = seg is HiitSegment.Done || idx >= doneIndex

        val phaseRemaining = when {
            isDone -> 0
            else -> remainingSec(r, nowEpochMs)
        }

        val totalRemaining = if (isDone) 0 else {
            val future = futureDurationSec[idx]
            (phaseRemaining + future).coerceAtLeast(0)
        }

        _state.value = ViewState.Loaded(
            seg.toUi(
                index = idx,
                phaseRemainingSec = phaseRemaining,
                totalRemainingSec = totalRemaining,
                isPaused = if (isDone) true else r.isPaused,
                next = segments.getOrNull((idx + 1).coerceAtMost(doneIndex)),
            )
        )
    }

    private fun remainingSec(r: RuntimeSession, nowEpochMs: Long): Int {
        val idx = r.segmentIndex.coerceIn(0, segments.lastIndex)
        val seg = segments[idx]
        val durMs = seg.durationSec.coerceAtLeast(0) * 1000L

        val stopAt = if (r.isPaused) (r.pausedAtEpochMs ?: nowEpochMs) else nowEpochMs
        val elapsedMs = max(0L, stopAt - r.segmentStartedAtEpochMs - r.accumulatedPausedMs)
        val remMs = max(0L, durMs - elapsedMs)

        return if (remMs == 0L) 0 else ((remMs + 999L) / 1000L).toInt()
    }

    private fun HiitSegment.toUi(
        index: Int,
        phaseRemainingSec: Int,
        totalRemainingSec: Int,
        isPaused: Boolean,
        next: HiitSegment?,
    ): HiitRunUiState {
        val phaseIndex =
            if (this is HiitSegment.Done) totalPhases else (index + 1).coerceAtMost(totalPhases)

        return when (this) {
            is HiitSegment.Prepare -> HiitRunUiState(
                phase = HiitPhase.Prepare,
                phaseLabel = "PREPARE",
                phaseRemaining = phaseRemainingSec,
                totalRemaining = totalRemainingSec,
                setIndex = 0,
                setsTotal = 0,
                nextLabel = next?.asNextLabel(),
                isPaused = isPaused,
                phaseIndex = phaseIndex,
                totalPhases = totalPhases,
                restIndex = 0,
                totalRest = 0,
            )

            is HiitSegment.Work -> HiitRunUiState(
                phase = HiitPhase.Work,
                phaseLabel = exerciseName,
                phaseRemaining = phaseRemainingSec,
                totalRemaining = totalRemainingSec,
                setIndex = setIndex,
                setsTotal = setsTotal,
                nextLabel = next?.asNextLabel(),
                isPaused = isPaused,
                phaseIndex = phaseIndex,
                totalPhases = totalPhases,
                restIndex = 0,
                totalRest = 0,
            )

            is HiitSegment.Rest -> HiitRunUiState(
                phase = HiitPhase.Rest,
                phaseLabel = "REST",
                phaseRemaining = phaseRemainingSec,
                totalRemaining = totalRemainingSec,
                setIndex = setIndex,
                setsTotal = setsTotal,
                nextLabel = next?.asNextLabel(),
                isPaused = isPaused,
                phaseIndex = phaseIndex,
                totalPhases = totalPhases,
                restIndex = restOrdinalInExercise.getOrElse(index) { 0 },
                totalRest = restTotalByExercise[exerciseName] ?: 0,
            )

            is HiitSegment.Done -> HiitRunUiState(
                phase = HiitPhase.Done,
                phaseLabel = "DONE",
                phaseRemaining = 0,
                totalRemaining = 0,
                setIndex = 0,
                setsTotal = 0,
                nextLabel = null,
                isPaused = true,
                phaseIndex = totalPhases,
                totalPhases = totalPhases,
                restIndex = 0,
                totalRest = 0,
            )
        }
    }

    private fun HiitSegment.asNextLabel(): String? {
        return when (this) {
            is HiitSegment.Prepare -> "Prepare"
            is HiitSegment.Work -> "$exerciseName ${formatSec(durationSec)}"
            is HiitSegment.Rest -> "Rest ${formatSec(durationSec)}"
            is HiitSegment.Done -> null
        }
    }

    // ---------------------------------------
    // Mapping entity <-> runtime
    // ---------------------------------------

    private fun HiitRunSessionEntity.toRuntime(): RuntimeSession {
        return RuntimeSession(
            workoutId = workoutId,
            segmentIndex = segmentIndex,
            segmentStartedAtEpochMs = segmentStartedAtEpochMs,
            isPaused = isPaused,
            pausedAtEpochMs = pausedAtEpochMs,
            accumulatedPausedMs = accumulatedPausedMs,
            isFinished = isFinished,
            createdAtEpochMs = createdAtEpochMs,
        )
    }

    private fun RuntimeSession.toEntity(updatedAt: Long): HiitRunSessionEntity {
        return HiitRunSessionEntity(
            workoutId = workoutId,
            segmentIndex = segmentIndex,
            segmentStartedAtEpochMs = segmentStartedAtEpochMs,
            isPaused = isPaused,
            pausedAtEpochMs = pausedAtEpochMs,
            accumulatedPausedMs = accumulatedPausedMs,
            isFinished = isFinished,
            createdAtEpochMs = createdAtEpochMs,
            updatedAtEpochMs = updatedAt,
        )
    }
}
