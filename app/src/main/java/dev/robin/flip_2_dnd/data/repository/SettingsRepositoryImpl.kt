package dev.robin.flip_2_dnd.data.repository

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import dev.robin.flip_2_dnd.domain.repository.SettingsRepository
import dev.robin.flip_2_dnd.presentation.settings.Sound
import dev.robin.flip_2_dnd.presentation.settings.VibrationPattern
import dev.robin.flip_2_dnd.services.FlipDetectorService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "SettingsRepositoryImpl"
private const val PREFS_NAME = "flip_2_dnd_settings"
private const val KEY_SCREEN_OFF_ONLY = "screen_off_only"
private const val KEY_VIBRATION = "vibration"
private const val KEY_SOUND = "sound"
private const val KEY_PRIORITY_DND = "priority_dnd"
private const val KEY_DND_ON_SOUND = "dnd_on_sound"
private const val KEY_DND_OFF_SOUND = "dnd_off_sound"
private const val KEY_USE_CUSTOM_VOLUME = "use_custom_volume"
private const val KEY_CUSTOM_VOLUME = "custom_volume"
private const val KEY_USE_CUSTOM_VIBRATION = "use_custom_vibration"
private const val KEY_CUSTOM_VIBRATION_STRENGTH = "custom_vibration_strength"
private const val KEY_DND_ON_VIBRATION = "dnd_on_vibration"
private const val KEY_DND_OFF_VIBRATION = "dnd_off_vibration"
private const val KEY_FLIP_SENSITIVITY = "flip_sensitivity"

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val appContext: Context
) : SettingsRepository {
    private val prefs: SharedPreferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val screenOffOnlyEnabled = MutableStateFlow(prefs.getBoolean(KEY_SCREEN_OFF_ONLY, true))
    private val vibrationEnabled = MutableStateFlow(prefs.getBoolean(KEY_VIBRATION, true))
    private val soundEnabled = MutableStateFlow(prefs.getBoolean(KEY_SOUND, true))
    private val priorityDndEnabled = MutableStateFlow(prefs.getBoolean(KEY_PRIORITY_DND, true))
    private val dndOnSound = MutableStateFlow(Sound.valueOf(prefs.getString(KEY_DND_ON_SOUND, Sound.SLUSH.name) ?: Sound.SLUSH.name))
    private val dndOffSound = MutableStateFlow(Sound.valueOf(prefs.getString(KEY_DND_OFF_SOUND, Sound.WHISTLE.name) ?: Sound.WHISTLE.name))
    private val useCustomVolume = MutableStateFlow(prefs.getBoolean(KEY_USE_CUSTOM_VOLUME, false))
    private val customVolume = MutableStateFlow(prefs.getFloat(KEY_CUSTOM_VOLUME, 0.5f))
    private val useCustomVibration = MutableStateFlow(prefs.getBoolean(KEY_USE_CUSTOM_VIBRATION, false))
    private val customVibrationStrength = MutableStateFlow(prefs.getFloat(KEY_CUSTOM_VIBRATION_STRENGTH, 0.5f))
    private val dndOnVibration = MutableStateFlow(VibrationPattern.valueOf(prefs.getString(KEY_DND_ON_VIBRATION, VibrationPattern.DOUBLE_PULSE.name) ?: VibrationPattern.DOUBLE_PULSE.name))
    private val dndOffVibration = MutableStateFlow(VibrationPattern.valueOf(prefs.getString(KEY_DND_OFF_VIBRATION, VibrationPattern.SINGLE_PULSE.name) ?: VibrationPattern.SINGLE_PULSE.name))
    private val flipSensitivity = MutableStateFlow(prefs.getFloat(KEY_FLIP_SENSITIVITY, 0.5f))

    private fun restartFlipDetectorService() {
        try {
            val serviceIntent = Intent(appContext, FlipDetectorService::class.java)
            appContext.stopService(serviceIntent)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                appContext.startForegroundService(serviceIntent)
            } else {
                appContext.startService(serviceIntent)
            }
            Log.d(TAG, "FlipDetectorService restarted successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error restarting FlipDetectorService: ${e.localizedMessage}", e)
        }
    }

    override fun getScreenOffOnlyEnabled(): Flow<Boolean> = screenOffOnlyEnabled

    override suspend fun setScreenOffOnlyEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SCREEN_OFF_ONLY, enabled).apply()
        screenOffOnlyEnabled.value = enabled
        restartFlipDetectorService()
    }

    override fun getVibrationEnabled(): Flow<Boolean> = vibrationEnabled

    override suspend fun setVibrationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VIBRATION, enabled).apply()
        vibrationEnabled.value = enabled
        restartFlipDetectorService()
    }

    override fun getSoundEnabled(): Flow<Boolean> = soundEnabled

    override suspend fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND, enabled).apply()
        soundEnabled.value = enabled
        restartFlipDetectorService()
    }

    override fun getPriorityDndEnabled(): Flow<Boolean> = priorityDndEnabled

    override suspend fun setPriorityDndEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PRIORITY_DND, enabled).apply()
        priorityDndEnabled.value = enabled
        restartFlipDetectorService()
    }

    override fun getDndOnSound(): Flow<Sound> = dndOnSound

    override suspend fun setDndOnSound(sound: Sound) {
        prefs.edit().putString(KEY_DND_ON_SOUND, sound.name).apply()
        dndOnSound.value = sound
        restartFlipDetectorService()
    }

    override fun getDndOffSound(): Flow<Sound> = dndOffSound

    override suspend fun setDndOffSound(sound: Sound) {
        prefs.edit().putString(KEY_DND_OFF_SOUND, sound.name).apply()
        dndOffSound.value = sound
        restartFlipDetectorService()
    }

    override fun getUseCustomVolume(): Flow<Boolean> = useCustomVolume

    override suspend fun setUseCustomVolume(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_USE_CUSTOM_VOLUME, enabled).apply()
        useCustomVolume.value = enabled
        restartFlipDetectorService()
    }

    override fun getCustomVolume(): Flow<Float> = customVolume

    override suspend fun setCustomVolume(volume: Float) {
        prefs.edit().putFloat(KEY_CUSTOM_VOLUME, volume).apply()
        customVolume.value = volume
        restartFlipDetectorService()
    }

    override fun getUseCustomVibration(): Flow<Boolean> = useCustomVibration

    override suspend fun setUseCustomVibration(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_USE_CUSTOM_VIBRATION, enabled).apply()
        useCustomVibration.value = enabled
        restartFlipDetectorService()
    }

    override fun getCustomVibrationStrength(): Flow<Float> = customVibrationStrength

    override suspend fun setCustomVibrationStrength(strength: Float) {
        prefs.edit().putFloat(KEY_CUSTOM_VIBRATION_STRENGTH, strength).apply()
        customVibrationStrength.value = strength
        restartFlipDetectorService()
    }

    override fun getDndOnVibration(): Flow<VibrationPattern> = dndOnVibration

    override suspend fun setDndOnVibration(pattern: VibrationPattern) {
        prefs.edit().putString(KEY_DND_ON_VIBRATION, pattern.name).apply()
        dndOnVibration.value = pattern
        restartFlipDetectorService()
    }

    override fun getDndOffVibration(): Flow<VibrationPattern> = dndOffVibration

    override suspend fun setDndOffVibration(pattern: VibrationPattern) {
        prefs.edit().putString(KEY_DND_OFF_VIBRATION, pattern.name).apply()
        dndOffVibration.value = pattern
        restartFlipDetectorService()
    }

    override fun getFlipSensitivity(): Flow<Float> = flipSensitivity

    override suspend fun setFlipSensitivity(sensitivity: Float) {
        prefs.edit().putFloat(KEY_FLIP_SENSITIVITY, sensitivity).apply()
        flipSensitivity.value = sensitivity
        restartFlipDetectorService()
    }
}
