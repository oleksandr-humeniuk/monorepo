package com.oho.hiit_timer.data

import com.oho.hiit_timer.QuickStartTimerViewModel
import com.oho.hiit_timer.data.storage.ExerciseEntity
import com.oho.hiit_timer.data.storage.HiitWorkoutsDao
import com.oho.hiit_timer.data.storage.WorkoutEntity
import com.oho.hiit_timer.data.storage.WorkoutWithExercises
import com.oho.hiit_timer.data.storage.policyFromDb
import com.oho.hiit_timer.data.storage.toDb
import com.oho.hiit_timer.domain.HiitExercise
import com.oho.hiit_timer.domain.HiitWorkout
import com.oho.hiit_timer.domain.QuickStartMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface HiitWorkoutsRepository {
    suspend fun ensureQuickStart(defaultState: QuickStartTimerViewModel.UiState)
    fun observeWorkout(id: String): Flow<HiitWorkout?>
    suspend fun upsert(workout: HiitWorkout, source: Int)

    suspend fun getWorkout(workoutId: String): HiitWorkout?
}


class HiitWorkoutsRepositoryImpl(
    private val dao: HiitWorkoutsDao,
    private val nowMs: () -> Long,
) : HiitWorkoutsRepository {
    override suspend fun ensureQuickStart(defaultState: QuickStartTimerViewModel.UiState) {
        val existing = dao.getWorkout(QuickStartMapper.QUICK_START_ID)
        if (existing != null) return

        val now = nowMs()
        val workout = QuickStartMapper.toWorkout(defaultState)
        val (w, ex) = workout.toEntitiesForInsert(
            source = QuickStartRepository.SOURCE_SYSTEM,
            createdAt = now,
            updatedAt = now,
        )
        dao.upsertWorkoutGraph(workout = w, exercises = ex)
    }

    override fun observeWorkout(id: String): Flow<HiitWorkout?> {
        return dao.observeWorkout(id).map { row ->
            row?.toDomain()
        }
    }

    override suspend fun upsert(workout: HiitWorkout, source: Int) {
        val now = nowMs()
        val existing = dao.getWorkout(workout.id)?.workout

        val createdAt = existing?.createdAt ?: now
        val resolvedSource = existing?.source ?: source

        val (w, ex) = workout.toEntitiesForUpsert(
            source = resolvedSource,
            createdAt = createdAt,
            updatedAt = now,
            isPinned = existing?.isPinned ?: false,
            isDeleted = existing?.isDeleted ?: false,
        )

        dao.upsertWorkoutGraph(w, ex)
    }

    override suspend fun getWorkout(workoutId: String): HiitWorkout? {
        return dao.getWorkout(workoutId)?.toDomain()
    }

}

private fun HiitWorkout.toEntitiesForInsert(
    source: Int,
    createdAt: Long,
    updatedAt: Long,
): Pair<WorkoutEntity, List<ExerciseEntity>> {
    return toEntitiesForUpsert(
        source = source,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isPinned = false,
        isDeleted = false,
    )
}

private fun HiitWorkout.toEntitiesForUpsert(
    source: Int,
    createdAt: Long,
    updatedAt: Long,
    isPinned: Boolean,
    isDeleted: Boolean,
): Pair<WorkoutEntity, List<ExerciseEntity>> {

    val w = WorkoutEntity(
        id = id,
        name = name,
        prepareSec = prepareSec,
        source = source,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isPinned = isPinned,
        isDeleted = isDeleted,
    )

    val ex = exercises.mapIndexed { index, e ->
        val (t, c) = e.restAfterLastWork.toDb()
        ExerciseEntity(
            id = e.id,
            workoutId = id,
            name = e.name,
            sets = e.sets,
            workSec = e.workSec,
            restSec = e.restSec,
            restAfterLastWorkType = t,
            restAfterLastWorkCustomSec = c,
            orderInWorkout = index,
        )
    }

    return w to ex
}

private fun WorkoutWithExercises.toDomain(): HiitWorkout {
    val sorted = exercises.sortedBy { it.orderInWorkout }
    return HiitWorkout(
        id = workout.id,
        name = workout.name,
        prepareSec = workout.prepareSec,
        exercises = sorted.map { e ->
            HiitExercise(
                id = e.id,
                name = e.name,
                sets = e.sets,
                workSec = e.workSec,
                restSec = e.restSec,
                restAfterLastWork = policyFromDb(
                    e.restAfterLastWorkType,
                    e.restAfterLastWorkCustomSec
                ),
            )
        }
    )
}