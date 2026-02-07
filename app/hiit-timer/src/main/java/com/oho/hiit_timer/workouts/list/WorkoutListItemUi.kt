package com.oho.hiit_timer.workouts.list

import androidx.compose.runtime.Immutable

@Immutable
data class WorkoutListItemUi(
    val id: String,
    val name: String,
    val blocksCount: Int,
    val totalDurationSec: Int,
)