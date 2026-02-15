package com.oho.hiit_timer.root

sealed interface HiitRootRoute {
    data object Tabs : HiitRootRoute
    data class Run(val workoutId: String) : HiitRootRoute

    data class CreateEditWorkout(val workoutId: String) : HiitRootRoute

    data class AddEditExercise(
        val id: String? = null // add null, edit not null
    ) : HiitRootRoute

    data class WorkoutDetails(
        val id: String
    ): HiitRootRoute
}