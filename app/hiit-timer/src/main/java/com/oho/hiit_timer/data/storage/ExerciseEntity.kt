package com.oho.hiit_timer.data.storage

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "hiit_exercises",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index("workoutId"),
        Index(value = ["workoutId", "orderInWorkout"], unique = true)
    ]
)
data class ExerciseEntity(
    @PrimaryKey val id: String, // UUID/ULID (або "$workoutId:$index" для system)
    val workoutId: String,

    val name: String,
    val sets: Int,
    val workSec: Int,
    val restSec: Int,

    /**
     * RestAfterLastWorkPolicy stored as:
     * 0 = SameAsRegular
     * 1 = None
     * 2 = Custom
     */
    val restAfterLastWorkType: Int,
    val restAfterLastWorkCustomSec: Int?, // only for Custom

    val orderInWorkout: Int, // stable ordering
)
