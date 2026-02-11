package com.oho.hiit_timer.workouts.add

import androidx.compose.runtime.Immutable
import com.oho.hiit_timer.domain.HiitExercise
import com.oho.hiit_timer.domain.totalDurationSec

@Immutable
sealed interface WorkoutBlockSpec {
    @Immutable
    data class Single(
        val sets: Int,
        val durationSec: Int,
    ) : WorkoutBlockSpec

    @Immutable
    data class Interval(
        val sets: Int,
        val workSec: Int,
        val restSec: Int,
    ) : WorkoutBlockSpec

    companion object {
        fun fromExercise(exercise: HiitExercise): WorkoutBlockSpec {
            return when {
                exercise.sets == 1 -> Single(1, durationSec = exercise.totalDurationSec())
                else -> Interval(
                    sets = exercise.sets,
                    workSec = exercise.workSec,
                    restSec = exercise.restSec
                )
            }
        }
    }
}