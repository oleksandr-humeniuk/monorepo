package com.oho.hiit_timer.data

import android.content.Context
import com.oho.hiit_timer.QuickStartTimerViewModel
import com.oho.hiit_timer.data.HiitWorkoutsRepository.Companion.SOURCE_SYSTEM
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

    fun observerWorkouts(source: Source): Flow<List<HiitWorkout>>
    suspend fun deleteExercise(exerciseId: String)
    suspend fun duplicateExercise(exerciseId: String, workoutId: String)
    suspend fun getExercise(exerciseId: String): HiitExercise?

    suspend fun deleteWorkout(workoutId: String)

    suspend fun duplicateWorkout(workoutId: String, newId: String, source: Int)


    enum class Source {
        System,
        User
    }

    companion object {
        const val TEMP_WORKOUT_ID = "temp_workout_id"
        const val SOURCE_SYSTEM = 0
        const val SOURCE_USER = 1
        const val SOURCE_PRESET = 2
    }
}


class HiitWorkoutsRepositoryImpl(
    private val dao: HiitWorkoutsDao,
    private val nowMs: () -> Long,
    private val context: Context
) : HiitWorkoutsRepository {
    override suspend fun ensureQuickStart(defaultState: QuickStartTimerViewModel.UiState) {
        val existing = dao.getWorkout(QuickStartMapper.QUICK_START_ID)
        if (existing != null) return

        val now = nowMs()
        val workout = QuickStartMapper.toWorkout(context, defaultState)
        val (w, ex) = workout.toEntitiesForInsert(
            source = SOURCE_SYSTEM,
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

    override fun observerWorkouts(source: HiitWorkoutsRepository.Source): Flow<List<HiitWorkout>> {
        return dao.observeWorkouts()
            .map { workouts ->
                workouts.map { workout -> workout.toDomain() }
            }
    }

    override suspend fun deleteExercise(exerciseId: String) {
        dao.deleteExercise(exerciseId)
    }

    override suspend fun duplicateExercise(exerciseId: String, workoutId: String) {
        dao.duplicateExercise(
            exerciseId = exerciseId,
            workoutId = workoutId
        )
    }

    override suspend fun getExercise(exerciseId: String): HiitExercise? {
        return dao.queryExercise(exerciseId = exerciseId)?.toDomain()
    }

    override suspend fun deleteWorkout(workoutId: String) {
        dao.deleteWorkout(workoutId)
    }

    override suspend fun duplicateWorkout(
        workoutId: String,
        newId: String,
        source: Int
    ) {
        dao.duplicateWorkout(
            workoutId = workoutId,
            newId = newId,
            source = source
        )
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
        source = source,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isPinned = isPinned,
        isDeleted = isDeleted,
    )

    val ex = exercises.mapIndexed { index, e ->
        e.toDb(
            workoutId = id,
            orderInWorkout = index
        )
    }

    return w to ex
}

fun HiitExercise.toDb(
    workoutId: String,
    orderInWorkout: Int
): ExerciseEntity {
    val e = this
    val (t, c) = e.restAfterLastWork.toDb()
    return ExerciseEntity(
        id = e.id,
        workoutId = workoutId,
        name = e.name,
        sets = e.sets,
        workSec = e.workSec,
        restSec = e.restSec,
        restAfterLastWorkType = t,
        restAfterLastWorkCustomSec = c,
        orderInWorkout = orderInWorkout,
    )
}

private fun WorkoutWithExercises.toDomain(prepareSec: Int = 10): HiitWorkout {
    val sorted = exercises.sortedBy { it.orderInWorkout }
    return HiitWorkout(
        id = workout.id,
        name = workout.name,
        prepareSec = prepareSec,
        exercises = sorted.map { e ->
            e.toDomain()
        }
    )
}

private fun ExerciseEntity.toDomain(): HiitExercise {
    val e = this
    return HiitExercise(
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