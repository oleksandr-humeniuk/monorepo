package com.oho.hiit_timer.data.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface HiitWorkoutsDao {

    @Transaction
    @Query("SELECT * FROM hiit_workouts WHERE isDeleted = 0 ORDER BY source ASC, updatedAt DESC")
    fun observeWorkouts(): Flow<List<WorkoutWithExercises>>

    @Transaction
    @Query("SELECT * FROM hiit_workouts WHERE id = :id LIMIT 1")
    suspend fun getWorkout(id: String): WorkoutWithExercises?

    @Transaction
    @Query("SELECT * FROM hiit_workouts WHERE id = :id LIMIT 1")
    fun observeWorkout(id: String): Flow<WorkoutWithExercises?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWorkout(workout: WorkoutEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertExercises(exercises: List<ExerciseEntity>)

    @Query("DELETE FROM hiit_exercises WHERE workoutId = :workoutId")
    suspend fun deleteExercisesByWorkout(workoutId: String)

    @Transaction
    suspend fun upsertWorkoutGraph(
        workout: WorkoutEntity,
        exercises: List<ExerciseEntity>,
    ) {
        upsertWorkout(workout)
        deleteExercisesByWorkout(workout.id)
        upsertExercises(exercises)
    }
}
