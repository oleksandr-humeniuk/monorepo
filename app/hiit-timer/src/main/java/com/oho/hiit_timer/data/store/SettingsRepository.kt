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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class SettingsRepository(
    private val store: DataStore<Preferences>,
) {

    private object Defaults {
        const val PREPARE_SEC = 5
        const val SHOW_TOTAL_REMAINING = true
        const val AUTO_START_NEXT_PHASE = true
        const val SOUND_ENABLED = true
        const val VOLUME = 0.7f
        const val VIBRATION_ENABLED = true
        val WORK_SOUND = Sound.RingBell
        val REST_SOUND = Sound.Whistle
        val DONE_SOUND = Sound.Whistle
        val SOUND_MODE = SoundMode.Sound
        val THEME_MODE = ThemeMode.Dark
        const val KEEP_SCREEN_ON = true
    }

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
        val SOUND_MODE = stringPreferencesKey("sound_mode")

        val THEME_MODE = stringPreferencesKey("theme_mode")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
    }

    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
    val hiitPreferences: StateFlow<HiitPreferences> = store.data.map { prefs ->
        HiitPreferences(
            defaultPrepareSec = prefs[Keys.DEFAULT_PREPARE_SEC] ?: Defaults.PREPARE_SEC,
            showTotalRemaining = prefs[Keys.SHOW_TOTAL_REMAINING] ?: Defaults.SHOW_TOTAL_REMAINING,
            autoStartNextPhase = prefs[Keys.AUTO_START_NEXT_PHASE] ?: Defaults.AUTO_START_NEXT_PHASE,

            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: Defaults.SOUND_ENABLED,
            volume = (prefs[Keys.VOLUME] ?: Defaults.VOLUME).coerceIn(0f, 1f),
            vibrationEnabled = prefs[Keys.VIBRATION_ENABLED] ?: Defaults.VIBRATION_ENABLED,
            workSound = prefs[Keys.WORK_SOUND].toSoundOrDefault(Defaults.WORK_SOUND),
            restSound = prefs[Keys.REST_SOUND].toSoundOrDefault(Defaults.REST_SOUND),
            doneSound = prefs[Keys.DONE_SOUND].toSoundOrDefault(Defaults.DONE_SOUND),
            soundMode = prefs[Keys.SOUND_MODE].toSoundModeOrDefault(Defaults.SOUND_MODE),

            themeMode = prefs[Keys.THEME_MODE].toThemeModeOrDefault(Defaults.THEME_MODE),
            keepScreenOn = prefs[Keys.KEEP_SCREEN_ON] ?: Defaults.KEEP_SCREEN_ON,
        )
    }.stateIn(
        scope, SharingStarted.Eagerly, HiitPreferences(
            defaultPrepareSec = Defaults.PREPARE_SEC,
            showTotalRemaining = Defaults.SHOW_TOTAL_REMAINING,
            autoStartNextPhase = Defaults.AUTO_START_NEXT_PHASE,
            soundEnabled = Defaults.SOUND_ENABLED,
            volume = Defaults.VOLUME,
            vibrationEnabled = Defaults.VIBRATION_ENABLED,
            workSound = Defaults.WORK_SOUND,
            restSound = Defaults.REST_SOUND,
            doneSound = Defaults.DONE_SOUND,
            soundMode = Defaults.SOUND_MODE,
            themeMode = Defaults.THEME_MODE,
            keepScreenOn = Defaults.KEEP_SCREEN_ON
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

    suspend fun setWorkSound(value: Sound) {
        store.edit { it[Keys.WORK_SOUND] = value.value }
    }

    suspend fun setRestSound(value: Sound) {
        store.edit { it[Keys.REST_SOUND] = value.value }
    }

    suspend fun setDoneSound(value: Sound) {
        store.edit { it[Keys.DONE_SOUND] = value.value }
    }

    suspend fun setSoundMode(value: SoundMode) {
        store.edit { it[Keys.SOUND_MODE] = value.value }
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
    val soundMode: SoundMode,
    val themeMode: ThemeMode,
    val keepScreenOn: Boolean,
)

enum class Sound(val value: String) {
    Whistle("whistle"),
    RingBell("ring_bell"),
}

enum class SoundMode(val value: String) {
    Sound("sound"),
    TTS("tts"),
}

private fun String?.toThemeModeOrDefault(default: ThemeMode): ThemeMode {
    return runCatching { ThemeMode.entries.first { it.value == this } }.getOrDefault(default)
}

private fun String?.toSoundOrDefault(default: Sound): Sound {
    return runCatching { Sound.entries.first { it.value == this } }.getOrDefault(default)
}

private fun String?.toSoundModeOrDefault(default: SoundMode): SoundMode {
    return runCatching { SoundMode.entries.first { it.value == this } }.getOrDefault(default)
}