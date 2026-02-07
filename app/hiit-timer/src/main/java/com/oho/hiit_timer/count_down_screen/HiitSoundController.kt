package com.oho.hiit_timer.count_down_screen

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.annotation.RawRes
import com.oho.hiit_timer.domain.HiitSegment
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicBoolean

/**
 * HIIT sound controller (SoundPool-based, deterministic, low-latency).
 *
 * Plays:
 * - Work start
 * - Rest start
 * - Optional last-seconds countdown beeps (e.g. 3-2-1)
 * - Workout finished
 *
 * Contract:
 * - Call [onSegmentChanged] exactly when the current segment index changes.
 * - Call [onTick] from your ticker if countdown is enabled.
 *
 * Determinism:
 * - Each cue is played at most once per segment transition / second boundary.
 * - If sounds are not loaded yet, playback is skipped (no delayed surprise).
 */
class HiitSoundController(
    context: Context,
    private val config: Config,
) {

    data class Config(
        @RawRes val workStartRes: Int,
        @RawRes val restStartRes: Int,
        @RawRes val workoutFinishedRes: Int,
        val volume: Float = 1.0f, // 0..1
        val enabled: Boolean = true,
        val countdown: Countdown? = null,
    ) {
        data class Countdown(
            @RawRes val beepRes: Int,
            val seconds: Set<Int> = setOf(3, 2, 1),
            val volumeMultiplier: Float = 0.9f,
            /**
             * If true, countdown beeps will also play during Prepare.
             * If false, only Work/Rest segments get countdown.
             */
            val includePrepare: Boolean = false,
        )
    }

    private val appContext = context.applicationContext

    private val scope = CoroutineScope(Dispatchers.Main.immediate)
    private val mutex = Mutex()

    private val soundPool: SoundPool
    private val loaded = AtomicBoolean(false)

    // SoundPool sample ids
    private var idWorkStart: Int = 0
    private var idRestStart: Int = 0
    private var idFinished: Int = 0
    private var idCountdown: Int = 0

    // Dedupe state
    private var lastIndex: Int = -1
    private var lastPhaseRemainingSec: Int? = null
    private var lastSecondBeeped: Int? = null
    private var finishedPlayedForRun: Boolean = false

    private var loadJob: Job? = null

    init {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(attrs)
            .build()

        // Async preload.
        loadJob = scope.launch { preload() }
    }

    suspend fun preload() {
        if (!config.enabled) return
        mutex.withLock {
            if (loaded.get()) return

            idWorkStart = soundPool.load(appContext, config.workStartRes, 1)
            idRestStart = soundPool.load(appContext, config.restStartRes, 1)
            idFinished = soundPool.load(appContext, config.workoutFinishedRes, 1)
            idCountdown = config.countdown?.let { soundPool.load(appContext, it.beepRes, 1) } ?: 0

            val required = buildSet {
                add(idWorkStart)
                add(idRestStart)
                add(idFinished)
                if (idCountdown != 0) add(idCountdown)
            }

            awaitLoaded(required)
            loaded.set(true)
        }
    }

    /**
     * Call when the current segment index changes (auto-advance OR manual jump).
     *
     * @param next next segment (non-null)
     */
    fun onSegmentChanged(
        nextIndex: Int,
        next: HiitSegment,
        isPaused: Boolean,
    ) {
        if (!config.enabled) return
        if (isPaused) return

        // Dedupe strictly on index change.
        if (nextIndex == lastIndex) return
        lastIndex = nextIndex

        // Play start cues for the segment we entered.
        when (next) {
            is HiitSegment.Work -> play(idWorkStart, config.volume)
            is HiitSegment.Rest -> play(idRestStart, config.volume)
            is HiitSegment.Done -> playFinishedOnce()
            is HiitSegment.Prepare -> {
                // no cue by default
            }
        }

        // Reset countdown state for new segment.
        lastPhaseRemainingSec = null
        lastSecondBeeped = null

        // If we reached Done, stop countdown beeps anyway.
        if (next is HiitSegment.Done) {
            lastPhaseRemainingSec = 0
            lastSecondBeeped = null
        }
    }

    /**
     * Optional: call from your ticker to support countdown beeps (3-2-1).
     * Safe to call every 100ms.
     */
    fun onTick(
        currentIndex: Int,
        currentSegment: HiitSegment,
        phaseRemainingSec: Int,
        isPaused: Boolean,
    ) {
        val cd = config.countdown ?: return
        if (!config.enabled) return
        if (isPaused) return
        if (currentSegment is HiitSegment.Done) return

        // If segment changed without onSegmentChanged, reset.
        if (currentIndex != lastIndex) {
            lastIndex = currentIndex
            lastPhaseRemainingSec = null
            lastSecondBeeped = null
        }

        val eligibleSegment = when (currentSegment) {
            is HiitSegment.Work, is HiitSegment.Rest -> true
            is HiitSegment.Prepare -> cd.includePrepare
            else -> false
        }
        if (!eligibleSegment) return

        // Dedupe on second boundary.
        if (lastPhaseRemainingSec == phaseRemainingSec) return
        lastPhaseRemainingSec = phaseRemainingSec

        if (phaseRemainingSec in cd.seconds && lastSecondBeeped != phaseRemainingSec) {
            lastSecondBeeped = phaseRemainingSec
            play(idCountdown, (config.volume * cd.volumeMultiplier).coerceIn(0f, 1f))
        }
    }

    /**
     * Call this when you start a new run (e.g. new workout).
     * If you reuse the controller across runs, you must reset the "finished" latch.
     */
    fun resetForNewRun() {
        finishedPlayedForRun = false
        lastIndex = -1
        lastPhaseRemainingSec = null
        lastSecondBeeped = null
    }

    fun release() {
        loadJob?.cancel()
        scope.cancel()
        soundPool.release()
    }

    // -------------------------
    // Internals
    // -------------------------

    private fun playFinishedOnce() {
        if (finishedPlayedForRun) return
        finishedPlayedForRun = true
        play(idFinished, config.volume)
    }

    private fun play(soundId: Int, volume: Float) {
        if (soundId == 0) return
        if (!loaded.get()) return // deterministic: skip if not ready
        val v = volume.coerceIn(0f, 1f)
        soundPool.play(soundId, v, v, /*priority*/1, /*loop*/0, /*rate*/1.0f)
    }

    private suspend fun awaitLoaded(required: Set<Int>) {
        if (required.isEmpty()) return

        val done = CompletableDeferred<Unit>()
        val ready = HashSet<Int>(required.size)

        val listener = SoundPool.OnLoadCompleteListener { _, sampleId, status ->
            if (status == 0 && sampleId in required) {
                synchronized(ready) {
                    ready.add(sampleId)
                    if (ready.containsAll(required) && !done.isCompleted) {
                        done.complete(Unit)
                    }
                }
            }
        }

        soundPool.setOnLoadCompleteListener(listener)
        try {
            done.await()
        } finally {
            soundPool.setOnLoadCompleteListener(null)
        }
    }
}
