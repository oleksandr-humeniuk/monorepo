package com.oho.hiit_timer.workouts.add_block

import androidx.compose.runtime.Immutable

@Immutable
data class IntervalBlockDraft(
    val name: String?,
    val sets: Int,
    val workSec: Int,
    val restSec: Int,
    /**
     * - If sets <= 1: ignored (can be null)
     * - If sets > 1: used as rest after last work
     */
    val lastRestSec: Int?,
)