package dev.robin.flip_2_dnd.domain.repository

import dev.robin.flip_2_dnd.presentation.settings.FlashlightPattern
import dev.robin.flip_2_dnd.presentation.settings.Sound
import dev.robin.flip_2_dnd.presentation.settings.VibrationPattern
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getScreenOffOnlyEnabled(): Flow<Boolean>
    suspend fun setScreenOffOnlyEnabled(enabled: Boolean)
    fun getVibrationEnabled(): Flow<Boolean>
    suspend fun setVibrationEnabled(enabled: Boolean)
    fun getSoundEnabled(): Flow<Boolean>
    suspend fun setSoundEnabled(enabled: Boolean)
    fun getPriorityDndEnabled(): Flow<Boolean>
    suspend fun setPriorityDndEnabled(enabled: Boolean)
    fun getDndOnSound(): Flow<Sound>
    suspend fun setDndOnSound(sound: Sound)
    fun getDndOffSound(): Flow<Sound>
    suspend fun setDndOffSound(sound: Sound)
    fun getUseCustomVolume(): Flow<Boolean>
    suspend fun setUseCustomVolume(enabled: Boolean)
    fun getCustomVolume(): Flow<Float>
    suspend fun setCustomVolume(volume: Float)
    fun getUseCustomVibration(): Flow<Boolean>
    suspend fun setUseCustomVibration(enabled: Boolean)
    fun getCustomVibrationStrength(): Flow<Float>
    suspend fun setCustomVibrationStrength(strength: Float)
    fun getDndOnVibration(): Flow<VibrationPattern>
    suspend fun setDndOnVibration(pattern: VibrationPattern)
    fun getDndOffVibration(): Flow<VibrationPattern>
    suspend fun setDndOffVibration(pattern: VibrationPattern)
    fun getFlipSensitivity(): Flow<Float>
    suspend fun setFlipSensitivity(sensitivity: Float)
    fun getDndOnCustomSoundUri(): Flow<String?>
    suspend fun setDndOnCustomSoundUri(uri: String?)
    fun getDndOffCustomSoundUri(): Flow<String?>
    suspend fun setDndOffCustomSoundUri(uri: String?)
    fun getNotificationsEnabled(): Flow<Boolean>
    suspend fun setNotificationsEnabled(enabled: Boolean)
    fun getHighSensitivityModeEnabled(): Flow<Boolean>
    suspend fun setHighSensitivityModeEnabled(enabled: Boolean)
    fun getBatterySaverOnFlipEnabled(): Flow<Boolean>
    suspend fun setBatterySaverOnFlipEnabled(enabled: Boolean)
    fun getActivationDelay(): Flow<Int>
    suspend fun setActivationDelay(seconds: Int)
    fun getFlashlightDetectionEnabled(): Flow<Boolean>
    suspend fun setFlashlightDetectionEnabled(enabled: Boolean)
    fun getMediaPlaybackDetectionEnabled(): Flow<Boolean>
    suspend fun setMediaPlaybackDetectionEnabled(enabled: Boolean)
    fun getHeadphoneDetectionEnabled(): Flow<Boolean>
    suspend fun setHeadphoneDetectionEnabled(enabled: Boolean)

    fun getFlashlightFeedbackEnabled(): Flow<Boolean>
    suspend fun setFlashlightFeedbackEnabled(enabled: Boolean)
    fun getDndOnFlashlightPattern(): Flow<FlashlightPattern>
    suspend fun setDndOnFlashlightPattern(pattern: FlashlightPattern)
    fun getDndOffFlashlightPattern(): Flow<FlashlightPattern>
    suspend fun setDndOffFlashlightPattern(pattern: FlashlightPattern)

    fun getDndScheduleEnabled(): Flow<Boolean>
    suspend fun setDndScheduleEnabled(enabled: Boolean)
    fun getDndScheduleStartTime(): Flow<String>
    suspend fun setDndScheduleStartTime(startTime: String)
    fun getDndScheduleEndTime(): Flow<String>
    suspend fun setDndScheduleEndTime(endTime: String)
    fun getDndScheduleDays(): Flow<Set<Int>>
    suspend fun setDndScheduleDays(days: Set<Int>)

    fun getSoundScheduleEnabled(): Flow<Boolean>
    suspend fun setSoundScheduleEnabled(enabled: Boolean)
    fun getSoundScheduleStartTime(): Flow<String>
    suspend fun setSoundScheduleStartTime(startTime: String)
    fun getSoundScheduleEndTime(): Flow<String>
    suspend fun setSoundScheduleEndTime(endTime: String)
    fun getSoundScheduleDays(): Flow<Set<Int>>
    suspend fun setSoundScheduleDays(days: Set<Int>)

    fun getVibrationScheduleEnabled(): Flow<Boolean>
    suspend fun setVibrationScheduleEnabled(enabled: Boolean)
    fun getVibrationScheduleStartTime(): Flow<String>
    suspend fun setVibrationScheduleStartTime(startTime: String)
    fun getVibrationScheduleEndTime(): Flow<String>
    suspend fun setVibrationScheduleEndTime(endTime: String)
    fun getVibrationScheduleDays(): Flow<Set<Int>>
    suspend fun setVibrationScheduleDays(days: Set<Int>)

    fun getAutoStartEnabled(): Flow<Boolean>
    suspend fun setAutoStartEnabled(enabled: Boolean)
}
