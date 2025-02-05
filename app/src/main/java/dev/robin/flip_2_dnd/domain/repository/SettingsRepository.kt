package dev.robin.flip_2_dnd.domain.repository

import dev.robin.flip_2_dnd.presentation.settings.Sound
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
}
