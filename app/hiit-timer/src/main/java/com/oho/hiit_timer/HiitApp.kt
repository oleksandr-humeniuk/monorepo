package com.oho.hiit_timer

import android.app.Application
import androidx.room.Room
import com.oho.hiit_timer.count_down_screen.HiitRunViewModel
import com.oho.hiit_timer.data.HiitWorkoutsRepository
import com.oho.hiit_timer.data.HiitWorkoutsRepositoryImpl
import com.oho.hiit_timer.data.QuickStartRepository
import com.oho.hiit_timer.data.storage.HiitDatabase
import com.oho.hiit_timer.tabs.TabsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androix.startup.KoinStartup
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinConfiguration
import org.koin.dsl.bind
import org.koin.dsl.module

@OptIn(KoinExperimentalAPI::class)
class HiitApp : Application(), KoinStartup {
    override fun onKoinStartup(): KoinConfiguration {
        return KoinConfiguration {
            androidContext(this@HiitApp)
            modules(appModule)
        }
    }
}

private val appModule = module {
    single<HiitDatabase> {
        Room.databaseBuilder(
            androidContext(),
            HiitDatabase::class.java,
            "hiit.db",
        ).build()
    }

    single {
        get<HiitDatabase>().hiitWorkoutDao()
    }

    single {
        get<HiitDatabase>().hiitSessionDao()
    }

    viewModelOf(::QuickStartTimerViewModel)
    viewModelOf(::HiitRunViewModel)
    viewModelOf(::TabsViewModel)
    factory {
        HiitWorkoutsRepositoryImpl(
            dao = get(),
            nowMs = { System.currentTimeMillis() },
        )
    } bind HiitWorkoutsRepository::class

    factoryOf(::QuickStartRepository)

}