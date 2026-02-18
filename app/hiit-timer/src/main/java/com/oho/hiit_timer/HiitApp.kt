package com.oho.hiit_timer

import android.app.Application
import androidx.room.Room
import com.oho.hiit_timer.count_down_screen.HiitRunViewModel
import com.oho.hiit_timer.data.HiitWorkoutsRepository
import com.oho.hiit_timer.data.HiitWorkoutsRepositoryImpl
import com.oho.hiit_timer.data.QuickStartRepository
import com.oho.hiit_timer.data.TempWorkoutRepository
import com.oho.hiit_timer.data.storage.HiitDatabase
import com.oho.hiit_timer.data.store.SettingsPreferences
import com.oho.hiit_timer.data.store.SettingsRepository
import com.oho.hiit_timer.root.HiitRootNavViewModel
import com.oho.hiit_timer.settings.SettingsViewModel
import com.oho.hiit_timer.settings.SoundSettingsViewModel
import com.oho.hiit_timer.tabs.TabsViewModel
import com.oho.hiit_timer.workouts.add.CreateEditWorkoutViewModel
import com.oho.hiit_timer.workouts.add_block.CreateEditIntervalViewModel
import com.oho.hiit_timer.workouts.list.WorkoutsListViewModel
import com.oho.hiit_timer.workouts.workout_details.WorkoutDetailsViewModel
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
    viewModelOf(::WorkoutsListViewModel)
    viewModelOf(::CreateEditWorkoutViewModel)
    viewModelOf(::HiitRootNavViewModel)
    viewModelOf(::CreateEditIntervalViewModel)
    viewModelOf(::WorkoutDetailsViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::SoundSettingsViewModel)

    factory {
        HiitWorkoutsRepositoryImpl(
            dao = get(),
            nowMs = { System.currentTimeMillis() },
            context = get(),
            settingsRepository = get(),
        )
    } bind HiitWorkoutsRepository::class

    factoryOf(::QuickStartRepository)
    factoryOf(::TempWorkoutRepository)

    single { SettingsPreferences(get()).dataStore }
    single {
        SettingsRepository(store = get())
    }
}
//TODO: reset min for rest and work to 0

//TODO: wheel picker https://github.com/commandiron/WheelPickerCompose