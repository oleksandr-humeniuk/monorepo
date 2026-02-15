package com.oho.hiit_timer.workouts.add

import androidx.compose.runtime.Immutable

@Immutable
data class WorkoutBlockUi(
    val id: String,
    val name: String,
    val spec: WorkoutBlockSpec,
) {
    val totalDurationSec: Int
        get() = when (spec) {
            is WorkoutBlockSpec.Interval -> spec.sets * (spec.workSec + spec.restSec)
        }
}