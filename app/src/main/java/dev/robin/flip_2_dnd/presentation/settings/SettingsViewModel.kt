package dev.robin.flip_2_dnd.presentation.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.robin.flip_2_dnd.presentation.settings.Sound
import dev.robin.flip_2_dnd.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val settingsRepository: SettingsRepository
) : AndroidViewModel(application) {

    private val _screenOffOnly = MutableStateFlow(false)
    val screenOffOnly = _screenOffOnly.asStateFlow()

    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabled = _soundEnabled.asStateFlow()

    private val _vibrationEnabled = MutableStateFlow(true)
    val vibrationEnabled = _vibrationEnabled.asStateFlow()

    private val _priorityDndEnabled = MutableStateFlow(false)
    val priorityDndEnabled = _priorityDndEnabled.asStateFlow()

    private val _dndOnSound = MutableStateFlow(Sound.HISS)
    val dndOnSound = _dndOnSound.asStateFlow()

    private val _dndOffSound = MutableStateFlow(Sound.HISS)
    val dndOffSound = _dndOffSound.asStateFlow()

    private val _useCustomVolume = MutableStateFlow(false)
    val useCustomVolume = _useCustomVolume.asStateFlow()

    private val _customVolume = MutableStateFlow(0.5f)
    val customVolume = _customVolume.asStateFlow()

    val availableSounds = Sound.values().toList()

    init {
        viewModelScope.launch {
            settingsRepository.getScreenOffOnlyEnabled().collect { enabled ->
                _screenOffOnly.value = enabled
            }
        }
        viewModelScope.launch {
            settingsRepository.getSoundEnabled().collect { enabled ->
                _soundEnabled.value = enabled
            }
        }
        viewModelScope.launch {
            settingsRepository.getVibrationEnabled().collect { enabled ->
                _vibrationEnabled.value = enabled
            }
        }
        viewModelScope.launch {
            settingsRepository.getPriorityDndEnabled().collect { enabled ->
                _priorityDndEnabled.value = enabled
            }
        }
        viewModelScope.launch {
            settingsRepository.getDndOnSound().collect { sound ->
                _dndOnSound.value = sound
            }
        }
        viewModelScope.launch {
            settingsRepository.getDndOffSound().collect { sound ->
                _dndOffSound.value = sound
            }
        }
        viewModelScope.launch {
            settingsRepository.getUseCustomVolume().collect { enabled ->
                _useCustomVolume.value = enabled
            }
        }
        viewModelScope.launch {
            settingsRepository.getCustomVolume().collect { volume ->
                _customVolume.value = volume
            }
        }
    }

    fun setScreenOffOnly(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setScreenOffOnlyEnabled(enabled)
        }
    }

    fun setPriorityDndEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setPriorityDndEnabled(enabled)
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSoundEnabled(enabled)
        }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setVibrationEnabled(enabled)
        }
    }

    fun setDndOnSound(sound: Sound) {
        viewModelScope.launch {
            settingsRepository.setDndOnSound(sound)
        }
    }

    fun setDndOffSound(sound: Sound) {
        viewModelScope.launch {
            settingsRepository.setDndOffSound(sound)
        }
    }

    fun setUseCustomVolume(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setUseCustomVolume(enabled)
        }
    }

    fun setCustomVolume(volume: Float) {
        viewModelScope.launch {
            settingsRepository.setCustomVolume(volume)
        }
    }
}
