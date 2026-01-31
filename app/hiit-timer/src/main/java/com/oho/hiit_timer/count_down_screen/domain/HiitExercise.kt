package com.oho.hiit_timer.count_down_screen.domain

data class HiitExercise(
    val id: String,
    val name: String,             // "присід", "жим", ...
    val sets: Int,
    val workSec: Int,
    val restSec: Int,
    val lastRestSec: Int? = null, // if null -> use restSec, if 0 -> no last rest
    val hasRestAfterLastWork: Boolean = true,
)