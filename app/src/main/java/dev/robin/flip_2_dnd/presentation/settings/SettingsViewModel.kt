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

    private val _dndOnSound = MutableStateFlow(Sound.SLUSH)
    val dndOnSound = _dndOnSound.asStateFlow()

    private val _dndOffSound = MutableStateFlow(Sound.WHISTLE)
    val dndOffSound = _dndOffSound.asStateFlow()

    private val _useCustomVolume = MutableStateFlow(false)
    val useCustomVolume = _useCustomVolume.asStateFlow()

    private val _customVolume = MutableStateFlow(0.5f)
    val customVolume = _customVolume.asStateFlow()

    val availableSounds = Sound.values().toList()
    val availableVibrationPatterns = VibrationPattern.values().toList()
    private val _useCustomVibration = MutableStateFlow(false)
    val useCustomVibration = _useCustomVibration.asStateFlow()

    private val _customVibrationStrength = MutableStateFlow(0.5f)
    val customVibrationStrength = _customVibrationStrength.asStateFlow()

    private val _dndOnVibration = MutableStateFlow(VibrationPattern.DOUBLE_PULSE)
    val dndOnVibration = _dndOnVibration.asStateFlow()

    private val _dndOffVibration = MutableStateFlow(VibrationPattern.SINGLE_PULSE)
    val dndOffVibration = _dndOffVibration.asStateFlow()

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
        viewModelScope.launch {
            settingsRepository.getUseCustomVibration().collect { enabled ->
                _useCustomVibration.value = enabled
            }
        }
        viewModelScope.launch {
            settingsRepository.getCustomVibrationStrength().collect { strength ->
                _customVibrationStrength.value = strength
            }
        }
        viewModelScope.launch {
            settingsRepository.getDndOnVibration().collect { pattern ->
                _dndOnVibration.value = pattern
            }
        }
        viewModelScope.launch {
            settingsRepository.getDndOffVibration().collect { pattern ->
                _dndOffVibration.value = pattern
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

    fun setUseCustomVibration(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setUseCustomVibration(enabled)
        }
    }

    fun setCustomVibrationStrength(strength: Float) {
        viewModelScope.launch {
            settingsRepository.setCustomVibrationStrength(strength)
        }
    }

    fun setDndOnVibration(pattern: VibrationPattern) {
        viewModelScope.launch {
            settingsRepository.setDndOnVibration(pattern)
        }
    }

    fun setDndOffVibration(pattern: VibrationPattern) {
        viewModelScope.launch {
            settingsRepository.setDndOffVibration(pattern)
        }
    }

    fun playSelectedSound(sound: Sound?) {
        if (sound == null || sound.soundResId == 0) {
            android.util.Log.d("SettingsViewModel", "No sound selected or invalid sound resource")
            return
        }
        
        val mediaPlayer = android.media.MediaPlayer.create(getApplication(), sound.soundResId)
        if (mediaPlayer == null) {
            android.util.Log.e("SettingsViewModel", "Failed to create MediaPlayer for sound: ${sound.name}")
            return
        }
        
        mediaPlayer.setVolume(
            if (useCustomVolume.value) customVolume.value else 1f,
            if (useCustomVolume.value) customVolume.value else 1f
        )
        mediaPlayer.setOnCompletionListener { mp -> mp.release() }
        mediaPlayer.start()
    }

    fun playSelectedVibration(pattern: VibrationPattern) {
        val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vibratorManager = getApplication<Application>().getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as android.os.VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getApplication<Application>().getSystemService(android.content.Context.VIBRATOR_SERVICE) as android.os.Vibrator
        }

        try {
            val useCustomVibration = useCustomVibration.value
            val customStrength = if (useCustomVibration) {
                customVibrationStrength.value
            } else {
                1.0f
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val baseAmplitude = (255 * customStrength).toInt().coerceIn(1, 255)
                val amplitudes = IntArray(pattern.pattern.size) { index ->
                    if (index % 2 == 0) 0 else baseAmplitude
                }
                
                val adjustedPattern = pattern.pattern.map { duration ->
                    duration.coerceAtLeast(50L)
                }.toLongArray()
                
                vibrator.vibrate(android.os.VibrationEffect.createWaveform(adjustedPattern, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern.pattern, -1)
            }
        } catch (e: Exception) {
            android.util.Log.e("SettingsViewModel", "Error during vibration: ${e.message}", e)
        }
    }
}
