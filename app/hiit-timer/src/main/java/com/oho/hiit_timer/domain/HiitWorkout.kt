package com.oho.hiit_timer.domain

data class HiitWorkout(
    val id: String,
    val name: String,
    val prepareSec: Int,
    val exercises: List<HiitExercise>,
)