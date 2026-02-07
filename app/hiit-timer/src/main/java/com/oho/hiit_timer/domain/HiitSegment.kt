package com.oho.hiit_timer.domain

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
        val exerciseName: String,
        val setIndex: Int,
        val setsTotal: Int,
        override val durationSec: Int,
    ) : HiitSegment()


    data class Done(override val durationSec: Int = 0) : HiitSegment()
}
