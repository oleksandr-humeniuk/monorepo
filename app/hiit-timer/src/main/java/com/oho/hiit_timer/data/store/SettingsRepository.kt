package com.oho.hiit_timer.data.store

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.oho.hiit_timer.settings.SettingsViewModel.ThemeMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class SettingsRepository(
    private val store: DataStore<Preferences>,
) {

    private object Keys {
        val DEFAULT_PREPARE_SEC = intPreferencesKey("default_prepare_sec")
        val SHOW_TOTAL_REMAINING = booleanPreferencesKey("show_total_remaining")
        val AUTO_START_NEXT_PHASE = booleanPreferencesKey("auto_start_next_phase")

        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val VOLUME = floatPreferencesKey("volume") // 0..1
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val WORK_SOUND = stringPreferencesKey("work_sound")
        val REST_SOUND = stringPreferencesKey("rest_sound")
        val DONE_SOUND = stringPreferencesKey("done_sound")

        val THEME_MODE = stringPreferencesKey("theme_mode")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
    }

    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
    val hiitPreferences: Flow<HiitPreferences> = store.data.map { prefs ->
        HiitPreferences(
            defaultPrepareSec = prefs[Keys.DEFAULT_PREPARE_SEC] ?: 5,
            showTotalRemaining = prefs[Keys.SHOW_TOTAL_REMAINING] ?: true,
            autoStartNextPhase = prefs[Keys.AUTO_START_NEXT_PHASE] ?: true,

            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
            volume = (prefs[Keys.VOLUME] ?: 0.7f).coerceIn(0f, 1f),
            vibrationEnabled = prefs[Keys.VIBRATION_ENABLED] ?: true,
            workSound = prefs[Keys.WORK_SOUND].toSoundOrDefault(Sound.RingBeLl),
            restSound = prefs[Keys.REST_SOUND].toSoundOrDefault(Sound.Whistle),
            doneSound = prefs[Keys.DONE_SOUND].toSoundOrDefault(Sound.Whistle),

            themeMode = prefs[Keys.THEME_MODE].toThemeModeOrDefault(ThemeMode.Dark),
            keepScreenOn = prefs[Keys.KEEP_SCREEN_ON] ?: true,
        )
    }.stateIn(
        scope, SharingStarted.Eagerly, HiitPreferences(
            defaultPrepareSec = 5,
            showTotalRemaining = true,
            autoStartNextPhase = true,
            soundEnabled = true,
            volume = 0.7f,
            vibrationEnabled = true,
            workSound = Sound.RingBeLl,
            restSound = Sound.Whistle,
            doneSound = Sound.Whistle,
            themeMode = ThemeMode.Dark,
            keepScreenOn = true
        )
    )

    // ---------------------------
    // Write operations
    // ---------------------------

    suspend fun setDefaultPrepareSec(value: Int) {
        store.edit { it[Keys.DEFAULT_PREPARE_SEC] = value.coerceAtLeast(0) }
    }

    suspend fun setShowTotalRemaining(value: Boolean) {
        store.edit { it[Keys.SHOW_TOTAL_REMAINING] = value }
    }

    suspend fun setAutoStartNextPhase(value: Boolean) {
        store.edit { it[Keys.AUTO_START_NEXT_PHASE] = value }
    }

    suspend fun setSoundEnabled(value: Boolean) {
        store.edit { it[Keys.SOUND_ENABLED] = value }
    }

    suspend fun setVolume(value: Float) {
        store.edit { it[Keys.VOLUME] = value.coerceIn(0f, 1f) }
    }

    suspend fun setVibrationEnabled(value: Boolean) {
        store.edit { it[Keys.VIBRATION_ENABLED] = value }
    }

    suspend fun setWorkSound(value: String) {
        store.edit { it[Keys.WORK_SOUND] = value }
    }

    suspend fun setRestSound(value: String) {
        store.edit { it[Keys.REST_SOUND] = value }
    }

    suspend fun setDoneSound(value: String) {
        store.edit { it[Keys.DONE_SOUND] = value }
    }

    suspend fun setThemeMode(value: ThemeMode) {
        store.edit { it[Keys.THEME_MODE] = value.value }
    }

    suspend fun setKeepScreenOn(value: Boolean) {
        store.edit { it[Keys.KEEP_SCREEN_ON] = value }
    }
}

data class HiitPreferences(
    val defaultPrepareSec: Int,
    val showTotalRemaining: Boolean,
    val autoStartNextPhase: Boolean,
    val soundEnabled: Boolean,
    val volume: Float,
    val vibrationEnabled: Boolean,
    val workSound: Sound,
    val restSound: Sound,
    val doneSound: Sound,
    val themeMode: ThemeMode,
    val keepScreenOn: Boolean,
)

enum class Sound(val value: String) {
    Whistle("whistle"),
    RingBeLl("ring_bell"),
}

private fun String?.toThemeModeOrDefault(default: ThemeMode): ThemeMode {
    return runCatching { ThemeMode.entries.first { it.value == this } }.getOrDefault(default)
}

private fun String?.toSoundOrDefault(default: Sound): Sound {
    return runCatching { Sound.entries.first { it.value == this } }.getOrDefault(default)
}