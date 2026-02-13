package com.oho.hiit_timer.domain

import android.content.Context
import com.oho.hiit_timer.QuickStartTimerViewModel
import com.oho.utils.R as timerR

object QuickStartMapper {
    const val QUICK_START_ID = "quick_start"
    private const val QUICK_EXERCISE_ID = "quick_start_ex"

    fun toWorkout(context: Context, state: QuickStartTimerViewModel.UiState): HiitWorkout {
        return HiitWorkout(
            id = QUICK_START_ID,
            name = context.getString(timerR.string.quick_start_workout_name),
            prepareSec = 0,
            exercises = listOf(
                HiitExercise(
                    id = QUICK_EXERCISE_ID,
                    name = context.getString(timerR.string.quick_start_exercise_name),
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
