package com.oho.hiit_timer.workouts.add

import androidx.compose.runtime.Immutable
import com.oho.hiit_timer.domain.HiitExercise
import com.oho.hiit_timer.domain.RestAfterLastWorkPolicy
import com.oho.hiit_timer.domain.totalDurationSec

@Immutable
sealed interface WorkoutBlockSpec {

    @Immutable
    data class Interval(
        val sets: Int,
        val workSec: Int,
        val restSec: Int,
        val lastRestSec: Int?,
        val total: Int,
    ) : WorkoutBlockSpec

    companion object {
        fun fromExercise(exercise: HiitExercise): WorkoutBlockSpec {
            return when {
                else -> Interval(
                    sets = exercise.sets,
                    workSec = exercise.workSec,
                    restSec = exercise.restSec,
                    lastRestSec = when (val last = exercise.restAfterLastWork) {
                        is RestAfterLastWorkPolicy.Custom -> last.seconds
                        RestAfterLastWorkPolicy.None -> null
                        RestAfterLastWorkPolicy.SameAsRegular -> exercise.restSec
                    },
                    total = exercise.totalDurationSec()
                )
            }
        }
    }
}