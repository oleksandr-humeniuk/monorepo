package com.oho.hiit_timer.data.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface HiitWorkoutsDao {

    @Transaction
    @Query("SELECT * FROM hiit_workouts WHERE source = 1 AND isDeleted = 0 ORDER BY source ASC, updatedAt DESC")
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertExercise(exercises: ExerciseEntity)

    @Query("SELECT COALESCE(MAX(orderInWorkout), -1) FROM hiit_exercises WHERE workoutId = :workoutId")
    suspend fun getMaxOrder(workoutId: String): Int

    @Transaction
    suspend fun upsertWorkoutGraph(
        workout: WorkoutEntity,
        exercises: List<ExerciseEntity>,
    ) {
        upsertWorkout(workout)
        deleteExercisesByWorkout(workout.id)
        upsertExercises(exercises)
    }

    @Query("DELETE FROM hiit_exercises WHERE id = :exerciseId")
    suspend fun deleteExercise(exerciseId: String)

    @Query("SELECT * FROM hiit_exercises WHERE  id =:exerciseId")
    suspend fun queryExercise(
        exerciseId: String
    ): ExerciseEntity?

    @Transaction
    suspend fun duplicateExercise(exerciseId: String, workoutId: String) {
        val toDuplicated = queryExercise( exerciseId = exerciseId) ?: return
        val currentMaxOrder = getMaxOrder(workoutId = workoutId)
        upsertExercise(
            toDuplicated.copy(
                id = UUID.randomUUID().toString(),
                orderInWorkout = currentMaxOrder + 1
            )
        )
    }
}
