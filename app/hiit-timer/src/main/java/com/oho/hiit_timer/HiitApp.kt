package com.oho.hiit_timer

import android.app.Application
import androidx.room.Room
import com.oho.hiit_timer.data.storage.HiitDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.androix.startup.KoinStartup
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinConfiguration
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

    viewModelOf(::IntervalTimerConfigViewModel)

}