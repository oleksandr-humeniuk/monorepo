package com.oho.hiit_timer.domain

/**
 * Policy for the rest phase after the LAST work set of an exercise.
 * This replaces the ambiguous lastRestSec + hasRestAfterLastWork combo.
 */
sealed interface RestAfterLastWorkPolicy {
    /** Use the regular restSec value. */
    data object SameAsRegular : RestAfterLastWorkPolicy

    /** No rest after last work. Exercise (or workout) ends on WORK. */
    data object None : RestAfterLastWorkPolicy

    /** Use a custom duration (seconds). If <= 0, treated as None. */
    data class Custom(val seconds: Int) : RestAfterLastWorkPolicy
}

data class HiitExercise(
    val id: String,
    val name: String,
    val sets: Int,
    val workSec: Int,
    val restSec: Int,

    /**
     * Rest policy after the last WORK set of this exercise.
     * - SameAsRegular: use restSec
     * - None: skip the last rest
     * - Custom: use provided seconds
     */
    val restAfterLastWork: RestAfterLastWorkPolicy = RestAfterLastWorkPolicy.SameAsRegular,
)
