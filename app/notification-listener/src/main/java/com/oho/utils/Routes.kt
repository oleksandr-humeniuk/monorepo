package com.oho.utils

sealed interface Routes {
    data object Gate : Routes
    data object Permissions : Routes
    data object Home : Routes
}