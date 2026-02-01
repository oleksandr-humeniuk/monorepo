package com.oho.hiit_timer.data.storage

import androidx.room.Embedded
import androidx.room.Relation

data class WorkoutWithExercises(
    @Embedded val workout: WorkoutEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "workoutId",
        entity = ExerciseEntity::class
    )
    val exercises: List<ExerciseEntity>,
)

