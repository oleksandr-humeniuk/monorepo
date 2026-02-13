package com.oho.hiit_timer.data

import android.content.Context
import com.oho.hiit_timer.QuickStartTimerViewModel
import com.oho.hiit_timer.data.HiitWorkoutsRepository.Companion.SOURCE_SYSTEM
import com.oho.hiit_timer.domain.QuickStartMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class QuickStartRepository(
    private val workouts: HiitWorkoutsRepository,
    private val context: Context
) {
    suspend fun ensure(defaultState: QuickStartTimerViewModel.UiState) {
        workouts.ensureQuickStart(defaultState)
    }

    fun observe(): Flow<QuickStartTimerViewModel.UiState?> {
        return workouts.observeWorkout(QuickStartMapper.QUICK_START_ID)
            .map { w -> w?.let { QuickStartMapper.fromWorkout(it) } }
    }

    suspend fun save(state: QuickStartTimerViewModel.UiState) {
        val workout = QuickStartMapper.toWorkout(context, state)
        workouts.upsert(workout = workout, source = SOURCE_SYSTEM)
    }

}


