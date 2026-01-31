package com.oho.hiit_timer.count_down_screen.domain

/** Logical phase in the session timeline. */
sealed class HiitSegment {
    abstract val durationSec: Int

    data class Prepare(override val durationSec: Int) : HiitSegment()
    data class Work(
        val exerciseName: String,
        val setIndex: Int,
        val setsTotal: Int,
        override val durationSec: Int,
    ) : HiitSegment()

    data class Rest(
        val nextExerciseName: String?, // optional hint
        val nextIsWork: Boolean,
        val setIndex: Int,
        val setsTotal: Int,
        override val durationSec: Int,
    ) : HiitSegment()

    data class Done(override val durationSec: Int = 0) : HiitSegment()
}