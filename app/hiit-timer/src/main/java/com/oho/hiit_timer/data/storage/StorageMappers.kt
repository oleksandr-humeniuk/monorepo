package com.oho.hiit_timer.data.storage

import com.oho.hiit_timer.domain.HiitWorkout
import com.oho.hiit_timer.domain.RestAfterLastWorkPolicy

private const val POLICY_SAME = 0
private const val POLICY_NONE = 1
private const val POLICY_CUSTOM = 2

fun RestAfterLastWorkPolicy.toDb(): Pair<Int, Int?> = when (this) {
    RestAfterLastWorkPolicy.SameAsRegular -> POLICY_SAME to null
    RestAfterLastWorkPolicy.None -> POLICY_NONE to null
    is RestAfterLastWorkPolicy.Custom -> POLICY_CUSTOM to this.seconds
}

fun policyFromDb(type: Int, custom: Int?): RestAfterLastWorkPolicy = when (type) {
    POLICY_SAME -> RestAfterLastWorkPolicy.SameAsRegular
    POLICY_NONE -> RestAfterLastWorkPolicy.None
    POLICY_CUSTOM -> RestAfterLastWorkPolicy.Custom(custom ?: 0)
    else -> RestAfterLastWorkPolicy.SameAsRegular
}


fun HiitWorkout.toEntities(
    source: Int,
    now: Long,
): Pair<WorkoutEntity, List<ExerciseEntity>> {
    val workoutEntity = WorkoutEntity(
        id = id,
        name = name,
        prepareSec = prepareSec,
        source = source,
        createdAt = now,
        updatedAt = now,
    )

    val exerciseEntities = exercises.mapIndexed { index, ex ->
        val (t, c) = ex.restAfterLastWork.toDb()
        ExerciseEntity(
            id = ex.id,
            workoutId = id,
            name = ex.name,
            sets = ex.sets,
            workSec = ex.workSec,
            restSec = ex.restSec,
            restAfterLastWorkType = t,
            restAfterLastWorkCustomSec = c,
            orderInWorkout = index,
        )
    }

    return workoutEntity to exerciseEntities
}
