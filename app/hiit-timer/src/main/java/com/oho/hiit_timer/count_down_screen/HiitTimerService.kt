package com.oho.hiit_timer.count_down_screen

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
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
    override fun onBind(intent: Intent?): IBinder = binder

    inner class LocalBinder : Binder() {
        fun service(): HiitRunService = this@HiitRunService
    }


    sealed interface Cmd {
        data class Start(val workoutId: String) : Cmd
        data object PauseResume : Cmd
        data object Next : Cmd
        data object Previous : Cmd
        data object Stop : Cmd
    }

    fun send(cmd: Cmd) {
        commands.trySend(cmd)
    }

    val state: StateFlow<HiitRunUiState> get() = _state


    private val svcJob = SupervisorJob()
    private val scope = CoroutineScope(svcJob + Dispatchers.Default)

    private val commands = Channel<Cmd>(capacity = Channel.BUFFERED)

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

    private val _state = MutableStateFlow(
        HiitRunUiState(
            phase = HiitPhase.Rest,
            totalRemaining = 0,
            phaseLabel = "REST",
            phaseRemaining = 0,
            setIndex = 0,
            setsTotal = 0,
            nextLabel = null,
            isPaused = false,
        )
    )

    override fun onCreate() {
        super.onCreate()
        Log.d("ZXC","create")
        NotificationHelper.ensureChannel(this)

        scope.launch {
            restoreIfAny()
            launch { commandLoop() }
            launch { tickerLoop() }
        }
    }

    override fun onDestroy() {
        soundController.release()
        svcJob.cancel()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // START_STICKY so system can recreate service. We'll restore from DB.
        Log.d("ZXC","started")
        scope.launch { restoreIfAny() }
        return START_STICKY
    }

    private suspend fun restoreIfAny() {
        val s = sessionDao.get() ?: return
        if (s.isFinished) {
            sessionDao.clear()
            return
        }

        val w = workoutsRepo.getWorkout(s.workoutId) ?: run {
            sessionDao.clear()
            return
        }
        applyWorkout(w)

        // Start foreground so it won’t die easily in background.
        ensureForeground()

        // Render immediately from restored snapshot.
        renderFromSession(s, nowEpochMs = System.currentTimeMillis())
    }

    private suspend fun commandLoop() {
        for (cmd in commands) {
            when (cmd) {
                is Cmd.Start -> handleStart(cmd.workoutId)
                Cmd.PauseResume -> handlePauseResume()
                Cmd.Next -> handleNext(manual = true)
                Cmd.Previous -> handlePrevious()
                Cmd.Stop -> handleStop()
            }
        }
    }

    private suspend fun handleStart(workoutId: String) {
        val w = workoutsRepo.getWorkout(workoutId) ?: return
        applyWorkout(w)
        soundController.resetForNewRun()

        val now = System.currentTimeMillis()
        val entity = HiitRunSessionEntity(
            workoutId = workoutId,
            segmentIndex = 0,
            segmentStartedAtEpochMs = now,
            isPaused = false,
            pausedAtEpochMs = null,
            accumulatedPausedMs = 0L,
            isFinished = false,
            createdAtEpochMs = now,
            updatedAtEpochMs = now,
        )
        sessionDao.upsert(entity)

        ensureForeground()
        renderFromSession(entity, nowEpochMs = now)

        // segment start cue
        soundController.onSegmentChanged(
            prevIndex = -1,
            prev = null,
            nextIndex = 0,
            next = segments[0],
            isPaused = false,
        )
    }

    private suspend fun handlePauseResume() {
        val s = sessionDao.get() ?: return
        if (s.isFinished) return

        val now = System.currentTimeMillis()

        val updated = if (!s.isPaused) {
            s.copy(
                isPaused = true,
                pausedAtEpochMs = now,
                updatedAtEpochMs = now,
            )
        } else {
            val pausedFor = max(0L, now - (s.pausedAtEpochMs ?: now))
            s.copy(
                isPaused = false,
                pausedAtEpochMs = null,
                accumulatedPausedMs = s.accumulatedPausedMs + pausedFor,
                updatedAtEpochMs = now,
            )
        }

        sessionDao.upsert(updated)
        renderFromSession(updated, nowEpochMs = now)
        updateNotification()
    }

    private suspend fun handleNext(manual: Boolean) {
        val s = sessionDao.get() ?: return
        if (s.isFinished) return

        val next = (s.segmentIndex + 1).coerceAtMost(doneIndex)
        if (next == s.segmentIndex) return

        val now = System.currentTimeMillis()
        val updated = s.copy(
            segmentIndex = next,
            segmentStartedAtEpochMs = now,
            isPaused = false,
            pausedAtEpochMs = null,
            accumulatedPausedMs = 0L,
            updatedAtEpochMs = now,
            isFinished = (next >= doneIndex) || (segments.getOrNull(next) is HiitSegment.Done)
        )
        sessionDao.upsert(updated)

        // sounds on transition
        soundController.onSegmentChanged(
            prevIndex = s.segmentIndex,
            prev = segments.getOrNull(s.segmentIndex),
            nextIndex = next,
            next = segments[next],
            isPaused = false,
        )

        renderFromSession(updated, nowEpochMs = now)
        updateNotification()

        if (updated.isFinished) {
            // Keep foreground notification as "Finished" or stop service (your choice).
            updateNotification(finished = true)
        }
    }

    private suspend fun handlePrevious() {
        val s = sessionDao.get() ?: return
        if (s.isFinished) return

        val prev = (s.segmentIndex - 1).coerceAtLeast(0)
        val now = System.currentTimeMillis()

        val updated = s.copy(
            segmentIndex = prev,
            segmentStartedAtEpochMs = now,
            isPaused = false,
            pausedAtEpochMs = null,
            accumulatedPausedMs = 0L,
            updatedAtEpochMs = now,
        )
        sessionDao.upsert(updated)

        soundController.onSegmentChanged(
            prevIndex = s.segmentIndex,
            prev = segments.getOrNull(s.segmentIndex),
            nextIndex = prev,
            next = segments[prev],
            isPaused = false,
        )

        renderFromSession(updated, nowEpochMs = now)
        updateNotification()
    }

    private suspend fun handleStop() {
        sessionDao.clear()
        _state.value = _state.value.copy(
            phase = HiitPhase.Done,
            totalRemaining = 0,
            phaseRemaining = 0,
            nextLabel = null,
            isPaused = true,
        )
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private suspend fun CoroutineScope.tickerLoop() {
        while (isActive) {
            delay(100L)

            val s = sessionDao.get() ?: continue
            if (s.isFinished) continue
            if (s.isPaused) {
                // Still update UI to keep it “alive” if bound
                renderFromSession(s, nowEpochMs = System.currentTimeMillis())
                continue
            }

            val now = System.currentTimeMillis()
            val remSec = remainingSec(s, now)

            // countdown tick beeps
            val idx = s.segmentIndex.coerceIn(0, segments.lastIndex)
            val seg = segments[idx]
            soundController.onTick(
                currentIndex = idx,
                currentSegment = seg,
                phaseRemainingSec = remSec,
                isPaused = false,
            )

            // auto-advance at boundary
            if (remSec <= 0) {
                handleNext(manual = false)
            } else {
                renderFromSession(s, nowEpochMs = now)
            }
        }
    }

    // ---------------------------------------
    // Planning + UI mapping
    // ---------------------------------------

    private fun applyWorkout(w: HiitWorkout) {
        workout = w
        segments = HiitPlanner.plan(w)

        doneIndex =
            segments.indexOfLast { it is HiitSegment.Done }.takeIf { it >= 0 } ?: segments.lastIndex
        totalPhases = max(0, doneIndex)

        // futureDurationSec[i] = sum of durations from i+1..doneIndex-1
        futureDurationSec = IntArray(segments.size) { 0 }
        var sum = 0
        for (i in (doneIndex - 1) downTo 0) {
            sum += segments[i + 1].durationSec.coerceAtLeast(0)
            futureDurationSec[i] = sum
        }

        // rest ordinals by exercise name
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

    private fun renderFromSession(s: HiitRunSessionEntity, nowEpochMs: Long) {
        val idx = s.segmentIndex.coerceIn(0, segments.lastIndex)
        val seg = segments[idx]
        val isDone = seg is HiitSegment.Done || idx >= doneIndex

        val phaseRemaining = when {
            isDone -> 0
            s.isPaused -> remainingSecPaused(s, nowEpochMs)
            else -> remainingSec(s, nowEpochMs)
        }

        val totalRemaining = if (isDone) 0 else {
            val future = futureDurationSec[idx]
            (phaseRemaining + future).coerceAtLeast(0)
        }

        _state.value = seg.toUi(
            index = idx,
            phaseRemainingSec = phaseRemaining,
            totalRemainingSec = totalRemaining,
            isPaused = if (isDone) true else s.isPaused,
            next = segments.getOrNull((idx + 1).coerceAtMost(doneIndex)),
        )
    }

    private fun remainingSecPaused(s: HiitRunSessionEntity, nowEpochMs: Long): Int {
        val pauseAt = s.pausedAtEpochMs ?: nowEpochMs
        return remainingSec(
            s.copy(isPaused = false), // treat as running but stop at pauseAt
            nowEpochMs = pauseAt
        )
    }

    private fun remainingSec(s: HiitRunSessionEntity, nowEpochMs: Long): Int {
        val idx = s.segmentIndex.coerceIn(0, segments.lastIndex)
        val seg = segments[idx]
        val durMs = seg.durationSec.coerceAtLeast(0) * 1000L

        val elapsedMs = max(0L, nowEpochMs - s.segmentStartedAtEpochMs - s.accumulatedPausedMs)
        val remMs = max(0L, durMs - elapsedMs)

        // ceil-ish seconds: (1..999ms) => 1
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
                phaseLabel = exerciseName ?: "Work",
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

    private fun ensureForeground() {
        val s = _state.value
        val notif = NotificationHelper.build(
            ctx = this,
            title = "HIIT Timer",
            text = "${s.phaseLabel}: ${formatSec(s.phaseRemaining)}",
            ongoing = true
        )
        startForeground(NotificationHelper.NOTIF_ID, notif)
    }

    private fun updateNotification(finished: Boolean = false) {
        val s = _state.value
        val text =
            if (finished) "Workout complete" else "${s.phaseLabel}: ${formatSec(s.phaseRemaining)}"
        NotificationHelper.notify(this, title = "HIIT Timer", text = text, ongoing = !finished)
    }
}
