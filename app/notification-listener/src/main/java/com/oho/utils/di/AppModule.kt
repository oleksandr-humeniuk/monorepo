package com.oho.utils.di

import androidx.room.Room
import com.oho.utils.apps_route.AppsViewModel
import com.oho.utils.database.NotificationDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    single<NotificationDatabase> {
        Room.databaseBuilder(
            androidContext(),
            NotificationDatabase::class.java,
            "notification.db",
        ).build()
    }
    viewModelOf(::AppsViewModel)
}