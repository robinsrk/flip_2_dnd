package dev.robin.flip_2_dnd.presentation.settings

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.robin.flip_2_dnd.domain.repository.SettingsRepository
import dev.robin.flip_2_dnd.presentation.main.MainState
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val settingsRepository: SettingsRepository
) : AndroidViewModel(application) {

    var state by mutableStateOf(MainState())
        private set

    init {
        viewModelScope.launch {
            settingsRepository.getScreenOffOnlyEnabled().collect { enabled ->
                state = state.copy(isScreenOffOnly = enabled)
            }
            settingsRepository.getVibrationEnabled().collect { enabled ->
                state = state.copy(isVibrationEnabled = enabled)
            }
            settingsRepository.getSoundEnabled().collect { enabled ->
                state = state.copy(isSoundEnabled = enabled)
            }
        }
    }

    fun onScreenOffOnlyChange(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setScreenOffOnlyEnabled(enabled)
        }
    }

    fun onVibrationChange(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setVibrationEnabled(enabled)
        }
    }

    fun onSoundChange(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSoundEnabled(enabled)
        }
    }
}
