package dev.robin.flip_2_dnd.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getScreenOffOnlyEnabled(): Flow<Boolean>
    suspend fun setScreenOffOnlyEnabled(enabled: Boolean)
    fun getVibrationEnabled(): Flow<Boolean>
    suspend fun setVibrationEnabled(enabled: Boolean)
    fun getSoundEnabled(): Flow<Boolean>
    suspend fun setSoundEnabled(enabled: Boolean)
}
