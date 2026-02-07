package com.oho.hiit_timer.domain

import com.oho.hiit_timer.QuickStartTimerViewModel

object QuickStartMapper {
    const val QUICK_START_ID = "quick_start"
    private const val QUICK_EXERCISE_ID = "quick_start_ex"

    fun toWorkout(state: QuickStartTimerViewModel.UiState): HiitWorkout {
        return HiitWorkout(
            id = QUICK_START_ID,
            name = "Quick start",
            prepareSec = 0,
            exercises = listOf(
                HiitExercise(
                    id = QUICK_EXERCISE_ID,
                    name = "Work",
                    sets = state.sets,
                    workSec = state.workSec,
                    restSec = state.restSec,
                    restAfterLastWork = if (state.skipLastRest) {
                        RestAfterLastWorkPolicy.None
                    } else {
                        RestAfterLastWorkPolicy.SameAsRegular
                    }
                )
            )
        )
    }

    fun fromWorkout(workout: HiitWorkout): QuickStartTimerViewModel.UiState? {
        val ex = workout.exercises.firstOrNull() ?: return null
        val skipLastRest = when (ex.restAfterLastWork) {
            RestAfterLastWorkPolicy.None -> true
            RestAfterLastWorkPolicy.SameAsRegular -> false
            is RestAfterLastWorkPolicy.Custom -> false // для quick start не очікуємо, але хай буде false
        }
        return QuickStartTimerViewModel.UiState(
            sets = ex.sets,
            workSec = ex.workSec,
            restSec = ex.restSec,
            skipLastRest = skipLastRest,
        )
    }
}
