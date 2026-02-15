package com.oho.hiit_timer.data

import com.oho.hiit_timer.data.storage.HiitWorkoutsDao
import com.oho.hiit_timer.domain.HiitExercise
import com.oho.hiit_timer.domain.HiitWorkout

class TempWorkoutRepository(
    private val workoutsRepository: HiitWorkoutsRepository,
    private val workoutsDao: HiitWorkoutsDao
) {
    suspend fun ensureExist() {
        workoutsRepository.upsert(
            workout = HiitWorkout(
                id = HiitWorkoutsRepository.TEMP_WORKOUT_ID,
                name = "",
                exercises = emptyList(),
                prepareSec = 0
            ),
            source = HiitWorkoutsRepository.SOURCE_SYSTEM,
        )
    }

    suspend fun upsertExercise(exercise: HiitExercise) {
        workoutsDao.upsertExercise(
            exercise.toDb(
                workoutId = HiitWorkoutsRepository.TEMP_WORKOUT_ID,
                orderInWorkout = workoutsDao.getMaxOrder(HiitWorkoutsRepository.TEMP_WORKOUT_ID) + 1
            )
        )
    }

    suspend fun duplicateToTemp(workoutId: String) {
        workoutsRepository.duplicateWorkout(
            workoutId = workoutId,
            newId = HiitWorkoutsRepository.TEMP_WORKOUT_ID,
            source = HiitWorkoutsRepository.SOURCE_SYSTEM
        )
    }
}