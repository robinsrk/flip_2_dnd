package com.cocoit.flip_2_dnd.domain.usecase

import com.cocoit.flip_2_dnd.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    fun getScreenOffOnlyEnabled(): Flow<Boolean> = 
        settingsRepository.getScreenOffOnlyEnabled()
    
    fun getVibrationEnabled(): Flow<Boolean> =
        settingsRepository.getVibrationEnabled()
    
    fun getSoundEnabled(): Flow<Boolean> =
        settingsRepository.getSoundEnabled()
}
