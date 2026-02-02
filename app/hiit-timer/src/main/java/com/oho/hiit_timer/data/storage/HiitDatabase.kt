package com.oho.hiit_timer.data.storage

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [WorkoutEntity::class, ExerciseEntity::class],
    version = 1,
    exportSchema = false //TODO: set to true before release
)
abstract class HiitDatabase : RoomDatabase() {
    abstract fun hiitWorkoutDao(): HiitWorkoutsDao
}