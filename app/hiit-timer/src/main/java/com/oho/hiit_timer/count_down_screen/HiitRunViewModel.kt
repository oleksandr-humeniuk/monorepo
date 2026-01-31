package com.oho.hiit_timer.count_down_screen

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oho.hiit_timer.count_down_screen.domain.HiitExercise
import com.oho.hiit_timer.count_down_screen.domain.HiitPlanner
import com.oho.hiit_timer.count_down_screen.domain.HiitSegment
import com.oho.hiit_timer.count_down_screen.domain.HiitWorkout
import com.oho.hiit_timer.count_down_screen.domain.RestAfterLastWorkPolicy
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

    // Segments metadata (precomputed once; stable source of truth)
    private val doneIndex: Int = segments.indexOfLast { it is HiitSegment.Done }.takeIf { it >= 0 }
        ?: segments.lastIndex

    /** Total phases excluding Done. */
    private val totalPhases: Int = max(0, doneIndex)

    /** futureDurationSec[i] = sum(durationSec of segments from (i+1) until Done exclusive). */
    private val futureDurationSec: IntArray = IntArray(segments.size) { 0 }

    /** Rest ordinal for each segment index (0 if not Rest). */
    private val restOrdinalInExercise: IntArray = IntArray(segments.size) { 0 }
    private val restTotalByExercise: Map<String, Int>

    init {
        // Precompute futureDurationSec and rest ordinals.
        var sum = 0
        for (i in (doneIndex - 1) downTo 0) {
            sum += segments[i + 1].durationSec.coerceAtLeast(0)
            futureDurationSec[i] = sum
        }
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

    // Single runtime snapshot (atomic source of truth for state transitions)
    private data class Runtime(
        val index: Int,                 // current segment index
        val segmentEndElapsedMs: Long,   // when current segment ends
        val isPaused: Boolean,
        val pausedAtElapsedMs: Long?,    // when pause started
        val pausedSegmentRemainingSec: Int?, // frozen remaining while paused
    )

    private var runtime: Runtime

    private var tickerJob: Job? = null

    private val _state = MutableStateFlow(
        HiitRunUiState(
            phase = HiitPhase.Rest,
            phaseLabel = "REST",
            phaseRemaining = 0,
            totalRemaining = 0,
            setIndex = 0,
            setsTotal = 0,
            nextLabel = null,
            isPaused = false,
        )
    )
    val state: StateFlow<HiitRunUiState> = _state.asStateFlow()

    init {
        val now = SystemClock.elapsedRealtime()
        val first = segments.firstOrNull() ?: HiitSegment.Done()
        val firstEnd = now + first.durationSec.coerceAtLeast(0) * 1000L

        runtime = Runtime(
            index = 0,
            segmentEndElapsedMs = firstEnd,
            isPaused = false,
            pausedAtElapsedMs = null,
            pausedSegmentRemainingSec = null,
        )

        render(now)
        startTicker()
    }

    fun onPauseResume() {
        val now = SystemClock.elapsedRealtime()
        val r = runtime

        if (isFinished(r)) return

        runtime = if (!r.isPaused) {
            // Pause: freeze remaining for current segment.
            val segRem = calcRemainingSec(now, r.segmentEndElapsedMs)
            r.copy(
                isPaused = true,
                pausedAtElapsedMs = now,
                pausedSegmentRemainingSec = segRem,
            )
        } else {
            // Resume: shift segment end forward by paused duration.
            val pausedForMs = max(0L, now - (r.pausedAtElapsedMs ?: now))
            r.copy(
                isPaused = false,
                pausedAtElapsedMs = null,
                pausedSegmentRemainingSec = null,
                segmentEndElapsedMs = r.segmentEndElapsedMs + pausedForMs,
            )
        }

        render(SystemClock.elapsedRealtime())
        ensureTicker()
    }

    fun onNext() {
        val now = SystemClock.elapsedRealtime()
        val nextIndex = (runtime.index + 1).coerceAtMost(doneIndex)
        jumpToIndex(nextIndex, now)
    }

    fun onPrevious() {
        val now = SystemClock.elapsedRealtime()
        val prevIndex = (runtime.index - 1).coerceAtLeast(0)
        jumpToIndex(prevIndex, now)
    }

    fun onClose() {
        if (!runtime.isPaused) {
            onPauseResume()
        }

        //TODO: exit flow
    }

    // ---------------------------
    // Internals
    // ---------------------------

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            // 100ms: smooth seconds display + reliable segment transitions.
            while (isActive) {
                val now = SystemClock.elapsedRealtime()
                if (!runtime.isPaused) {
                    advanceIfNeeded(now)
                }
                render(now)
                delay(100L)
            }
        }
    }

    private fun ensureTicker() {
        if (tickerJob?.isActive != true) startTicker()
    }

    /**
     * Deterministic transitions:
     * - No artificial holds.
     * - If time jumped forward (background), advance through multiple segments in one tick.
     * - Next segment end is computed from previous end (not from now) to avoid drift.
     */
    private fun advanceIfNeeded(now: Long) {
        var r = runtime
        if (isFinished(r)) return

        // Advance while we've crossed segment end.
        while (!r.isPaused && !isFinished(r) && now >= r.segmentEndElapsedMs) {
            val nextIndex = (r.index + 1).coerceAtMost(doneIndex)
            if (nextIndex == r.index) break

            val nextSeg = segments[nextIndex]
            val nextEnd = r.segmentEndElapsedMs + nextSeg.durationSec.coerceAtLeast(0) * 1000L

            r = r.copy(
                index = nextIndex,
                segmentEndElapsedMs = nextEnd,
            )
        }

        runtime = r
    }

    /**
     * Manual jump anchors the new segment start to now (user intent).
     * TotalRemaining is derived from (current remaining + futureDurationSec[index]).
     */
    private fun jumpToIndex(index: Int, now: Long) {
        val clamped = index.coerceIn(0, doneIndex)
        val seg = segments[clamped]

        runtime = Runtime(
            index = clamped,
            segmentEndElapsedMs = now + seg.durationSec.coerceAtLeast(0) * 1000L,
            isPaused = false,
            pausedAtElapsedMs = null,
            pausedSegmentRemainingSec = null,
        )

        render(now)
        ensureTicker()
    }

    private fun render(now: Long) {
        val r = runtime
        val idx = r.index.coerceIn(0, segments.lastIndex)
        val seg = segments[idx]

        val isDone = seg is HiitSegment.Done || idx >= doneIndex

        val phaseRemaining = when {
            isDone -> 0
            r.isPaused -> r.pausedSegmentRemainingSec ?: 0
            else -> calcRemainingSec(now, r.segmentEndElapsedMs)
        }

        val totalRemaining = if (isDone) {
            0
        } else {
            // Single source of truth:
            // total = currentRemaining + sum(future durations)
            val future = futureDurationSec[idx]
            (phaseRemaining + future).coerceAtLeast(0)
        }

        val ui = seg.toUi(
            index = idx,
            phaseRemainingSec = phaseRemaining,
            totalRemainingSec = totalRemaining,
            isPaused = if (isDone) true else r.isPaused,
            next = segments.getOrNull((idx + 1).coerceAtMost(doneIndex)),
        )

        _state.value = ui
    }

    private fun isFinished(r: Runtime): Boolean {
        val idx = r.index
        return idx >= doneIndex || segments.getOrNull(idx) is HiitSegment.Done
    }

    /**
     * "Ceil-ish" seconds so we don't show 00:00 too early.
     * For ms in (1..999) -> 1 sec.
     */
    private fun calcRemainingSec(nowElapsedMs: Long, endElapsedMs: Long): Int {
        if (nowElapsedMs >= endElapsedMs) return 0

        val ms = endElapsedMs - nowElapsedMs

        return ((ms + 999L) / 1000L).toInt()
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
                phaseLabel = exerciseName, // show exercise name instead of "WORK"
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
                totalRest = restTotalByExercise[this.exerciseName] ?: 0
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
                    restAfterLastWork = RestAfterLastWorkPolicy.None,
                ),
                HiitExercise(
                    id = "ex2",
                    name = "Тяга",
                    sets = 2,
                    workSec = 10,
                    restSec = 10,
                    restAfterLastWork = RestAfterLastWorkPolicy.SameAsRegular,
                ),
                HiitExercise(
                    id = "ex3",
                    name = "Жим",
                    sets = 2,
                    workSec = 10,
                    restSec = 10,
                    restAfterLastWork = RestAfterLastWorkPolicy.Custom(seconds = 10),
                ),
            )
        )
    }
}
