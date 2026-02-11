package com.oho.hiit_timer.data

import com.oho.hiit_timer.domain.HiitWorkout

class TempWorkoutRepository(
    private val workoutsRepository: HiitWorkoutsRepository
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
}