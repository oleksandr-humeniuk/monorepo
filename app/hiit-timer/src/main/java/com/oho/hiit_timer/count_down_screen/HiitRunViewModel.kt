package com.oho.hiit_timer.count_down_screen

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oho.hiit_timer.count_down_screen.domain.HiitExercise
import com.oho.hiit_timer.count_down_screen.domain.HiitPlanner
import com.oho.hiit_timer.count_down_screen.domain.HiitSegment
import com.oho.hiit_timer.count_down_screen.domain.HiitWorkout
import com.oho.hiit_timer.formatSec
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.max

class HiitRunViewModel : ViewModel() {

    // --- Mock workout (Quick Start example)
    private val workout: HiitWorkout = mockWorkout()

    private val segments: List<HiitSegment> = HiitPlanner.plan(workout)
    private val totalPlannedSec: Int = HiitPlanner.totalDurationSec(segments)

    // Session runtime state (monotonic time based)
    private data class Runtime(
        val index: Int,                  // current segment index
        val segmentEndElapsedMs: Long,    // when current segment ends
        val workoutEndElapsedMs: Long,    // when workout ends
        val isPaused: Boolean,
        val pausedAtElapsedMs: Long?,     // when pause started
        val pauseSegmentRemainingSec: Int?, // frozen remaining for segment while paused
        val pauseTotalRemainingSec: Int?,   // frozen remaining for workout while paused
    )

    private var runtime: Runtime

    private var tickerJob: Job? = null

    private val _state = MutableStateFlow(
        HiitRunUiState(
            phase = HiitPhase.Rest,
            phaseLabel = "REST",
            phaseRemaining = 10,
            totalRemaining = 60,
            setIndex = 1,
            setsTotal = 1,
            nextLabel = null,
            isPaused = false,
        )
    )
    val state: StateFlow<HiitRunUiState> = _state.asStateFlow()

    init {
        // Start session immediately (you can gate it behind explicit start if you want)
        val now = SystemClock.elapsedRealtime()
        val first = segments.first()
        val endSeg = now + first.durationSec * 1000L
        val endWorkout = now + totalPlannedSec * 1000L

        runtime = Runtime(
            index = 0,
            segmentEndElapsedMs = endSeg,
            workoutEndElapsedMs = endWorkout,
            isPaused = false,
            pausedAtElapsedMs = null,
            pauseSegmentRemainingSec = null,
            pauseTotalRemainingSec = null,
        )

        render(now)
        startTicker()
    }

    fun onPauseResume() {
        val now = SystemClock.elapsedRealtime()
        val r = runtime

        runtime = if (!r.isPaused) {
            // Pause: freeze remaining
            val segRem = calcRemainingSec(now, r.segmentEndElapsedMs)
            val totRem = calcRemainingSec(now, r.workoutEndElapsedMs)
            r.copy(
                isPaused = true,
                pausedAtElapsedMs = now,
                pauseSegmentRemainingSec = segRem,
                pauseTotalRemainingSec = totRem,
            )
        } else {
            // Resume: shift end times forward by paused duration
            val pausedForMs = max(0L, now - (r.pausedAtElapsedMs ?: now))
            r.copy(
                isPaused = false,
                pausedAtElapsedMs = null,
                pauseSegmentRemainingSec = null,
                pauseTotalRemainingSec = null,
                segmentEndElapsedMs = r.segmentEndElapsedMs + pausedForMs,
                workoutEndElapsedMs = r.workoutEndElapsedMs + pausedForMs,
            )
        }

        render(SystemClock.elapsedRealtime())
        ensureTicker()
    }

    fun onNext() {
        // Skip current segment -> move to next segment start
        val now = SystemClock.elapsedRealtime()
        val nextIndex = (runtime.index + 1).coerceAtMost(segments.lastIndex)
        jumpToIndex(nextIndex, now)
    }

    fun onPrevious() {
        // Go back to previous segment start (not "rewind within segment")
        val now = SystemClock.elapsedRealtime()
        val prevIndex = (runtime.index - 1).coerceAtLeast(0)
        jumpToIndex(prevIndex, now)
    }

    fun onClose() {
        // In v1: stop ticker; let UI navigate away.
        tickerJob?.cancel()
        tickerJob = null
    }

    // ---------------------------
    // Internals
    // ---------------------------

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            // Update often enough to hit second boundaries; UI will show seconds.
            while (isActive) {
                val now = SystemClock.elapsedRealtime()
                if (!runtime.isPaused) {
                    advanceIfNeeded(now)
                    render(now)
                } else {
                    // While paused, just keep state stable (no ticking needed),
                    // but it's fine to keep loop light.
                    render(now)
                }
                delay(100L)
            }
        }
    }

    private fun ensureTicker() {
        if (tickerJob?.isActive != true) startTicker()
    }

    private fun advanceIfNeeded(now: Long) {
        var r = runtime
        while (!r.isPaused && now >= r.segmentEndElapsedMs) {
            val nextIndex = (r.index + 1).coerceAtMost(segments.lastIndex)
            if (nextIndex == r.index) break

            val nextSeg = segments[nextIndex]
            val nextEnd = r.segmentEndElapsedMs + nextSeg.durationSec * 1000L

            r = r.copy(
                index = nextIndex,
                segmentEndElapsedMs = nextEnd,
            )
        }
        runtime = r
    }

    private fun jumpToIndex(index: Int, now: Long) {
        val clamped = index.coerceIn(0, segments.lastIndex)
        val seg = segments[clamped]

        // Rebuild runtime end times deterministically:
        // - segment ends at now + seg.duration
        // - workout end = now + remainingFromIndex
        val remainingWorkoutSec = remainingWorkoutFromIndexSec(clamped)
        runtime = Runtime(
            index = clamped,
            segmentEndElapsedMs = now + seg.durationSec * 1000L,
            workoutEndElapsedMs = now + remainingWorkoutSec * 1000L,
            isPaused = false,
            pausedAtElapsedMs = null,
            pauseSegmentRemainingSec = null,
            pauseTotalRemainingSec = null,
        )

        render(now)
        ensureTicker()
    }

    private fun remainingWorkoutFromIndexSec(index: Int): Int {
        var sum = 0
        for (i in index until segments.size) {
            sum += segments[i].durationSec
        }
        return sum
    }

    private fun render(now: Long) {
        val r = runtime
        val seg = segments[r.index]

        val phaseRemaining = if (r.isPaused) {
            r.pauseSegmentRemainingSec ?: 0
        } else {
            calcRemainingSec(now, r.segmentEndElapsedMs)
        }

        val totalRemaining = if (r.isPaused) {
            r.pauseTotalRemainingSec ?: 0
        } else {
            calcRemainingSec(now, r.workoutEndElapsedMs)
        }

        val ui = seg.toUi(
            phaseRemainingSec = phaseRemaining,
            totalRemainingSec = totalRemaining,
            isPaused = r.isPaused,
            next = segments.getOrNull(r.index + 1),
        )

        _state.value = ui
    }

    private fun calcRemainingSec(nowElapsedMs: Long, endElapsedMs: Long): Int {
        val ms = (endElapsedMs - nowElapsedMs).coerceAtLeast(0L)
        // ceil-ish so we don't show 00:00 too early
        return ((ms + 999L) / 1000L).toInt()
    }

    private fun HiitSegment.toUi(
        phaseRemainingSec: Int,
        totalRemainingSec: Int,
        isPaused: Boolean,
        next: HiitSegment?,
    ): HiitRunUiState {
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
            )

            is HiitSegment.Work -> HiitRunUiState(
                phase = HiitPhase.Work,
                phaseLabel = exerciseName, // вместо "WORK" показываем имя упражнения
                phaseRemaining = phaseRemainingSec,
                totalRemaining = totalRemainingSec,
                setIndex = setIndex,
                setsTotal = setsTotal,
                nextLabel = next?.asNextLabel(),
                isPaused = isPaused,
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


    private fun mockWorkout(): HiitWorkout {
        return HiitWorkout(
            id = "quick_start",
            name = "Quick Start",
            prepareSec = 5,
            exercises = listOf(
                HiitExercise(
                    id = "ex1",
                    name = "Присяд",
                    sets = 2,
                    workSec = 10,
                    restSec = 10,
                    lastRestSec = 10, //TODO without last rest sec not working              // last rest longer
                    hasRestAfterLastWork = true,   // set false if you want end on WORK
                ),
                HiitExercise(
                    id = "ex1",
                    name = "Жим",
                    sets = 2,
                    workSec = 10,
                    restSec = 10,
                    lastRestSec = 10,              // last rest longer
                    hasRestAfterLastWork = true,   // set false if you want end on WORK
                )
            )
        )
    }

}