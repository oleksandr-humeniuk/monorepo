package com.oho.hiit_timer.data.storage

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [WorkoutEntity::class, ExerciseEntity::class, HiitRunSessionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class HiitDatabase : RoomDatabase() {
    abstract fun hiitWorkoutDao(): HiitWorkoutsDao
    abstract fun hiitSessionDao(): HiitRunSessionDao
}