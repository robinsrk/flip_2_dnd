package dev.robin.flip_2_dnd.presentation.settings

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.robin.flip_2_dnd.data.model.GitHubRelease
import dev.robin.flip_2_dnd.domain.repository.SettingsRepository
import dev.robin.flip_2_dnd.domain.repository.UpdateRepository
import dev.robin.flip_2_dnd.presentation.main.MainState
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UpdateState(
    val isChecking: Boolean = false,
    val release: GitHubRelease? = null,
    val error: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val settingsRepository: SettingsRepository,
    private val updateRepository: UpdateRepository
) : AndroidViewModel(application) {

    var state by mutableStateOf(MainState())
        private set

    var updateState by mutableStateOf(UpdateState())
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

    fun checkForUpdates() {
        updateState = updateState.copy(isChecking = true, error = null, release = null)
        viewModelScope.launch {
            updateRepository.checkForUpdates()
                .onSuccess { release ->
                    updateState = updateState.copy(
                        isChecking = false,
                        release = release,
                        error = null
                    )
                }
                .onFailure { exception ->
                    updateState = updateState.copy(
                        isChecking = false,
                        release = null,
                        error = exception.message
                    )
                }
        }
    }
}
