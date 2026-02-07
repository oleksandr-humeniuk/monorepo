package com.oho.hiit_timer.root

sealed interface HiitRootRoute {
    data object Tabs : HiitRootRoute
    data class Run(val workoutId: String) : HiitRootRoute
}