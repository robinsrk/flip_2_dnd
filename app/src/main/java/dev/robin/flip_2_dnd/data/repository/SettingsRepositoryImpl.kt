package dev.robin.flip_2_dnd.data.repository

import android.content.Context
import android.content.SharedPreferences
import dev.robin.flip_2_dnd.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context
) : SettingsRepository {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val screenOffOnlyEnabled = MutableStateFlow(prefs.getBoolean(KEY_SCREEN_OFF_ONLY, false))
    private val vibrationEnabled = MutableStateFlow(prefs.getBoolean(KEY_VIBRATION, true))
    private val soundEnabled = MutableStateFlow(prefs.getBoolean(KEY_SOUND, false))

    override fun getScreenOffOnlyEnabled(): Flow<Boolean> = screenOffOnlyEnabled

    override suspend fun setScreenOffOnlyEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SCREEN_OFF_ONLY, enabled).apply()
        screenOffOnlyEnabled.value = enabled
    }

    override fun getVibrationEnabled(): Flow<Boolean> = vibrationEnabled

    override suspend fun setVibrationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VIBRATION, enabled).apply()
        vibrationEnabled.value = enabled
    }

    override fun getSoundEnabled(): Flow<Boolean> = soundEnabled

    override suspend fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND, enabled).apply()
        soundEnabled.value = enabled
    }

    companion object {
        private const val PREFS_NAME = "flip_2_dnd_settings"
        private const val KEY_SCREEN_OFF_ONLY = "screen_off_only"
        private const val KEY_VIBRATION = "vibration_enabled"
        private const val KEY_SOUND = "sound_enabled"
    }
}
