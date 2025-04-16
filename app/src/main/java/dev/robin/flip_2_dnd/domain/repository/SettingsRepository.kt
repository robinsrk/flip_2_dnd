package dev.robin.flip_2_dnd.domain.repository

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
}
