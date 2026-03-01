package dev.robin.flip_2_dnd.presentation.main

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.robin.flip_2_dnd.R
import dev.robin.flip_2_dnd.core.PhoneOrientation
import dev.robin.flip_2_dnd.core.DndRepository
import dev.robin.flip_2_dnd.core.FeedbackRepository
import dev.robin.flip_2_dnd.core.ScreenStateRepository
import dev.robin.flip_2_dnd.core.SettingsRepository
import dev.robin.flip_2_dnd.domain.usecase.GetOrientationUseCase
import dev.robin.flip_2_dnd.domain.usecase.GetSettingsUseCase
import dev.robin.flip_2_dnd.domain.usecase.ToggleDndUseCase
import dev.robin.flip_2_dnd.services.FlipDetectorService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getOrientationUseCase: GetOrientationUseCase,
    private val getSettingsUseCase: GetSettingsUseCase,
    private val toggleDndUseCase: ToggleDndUseCase,
    private val dndRepository: DndRepository,
    private val settingsRepository: SettingsRepository,
    private val feedbackRepository: FeedbackRepository,
    private val screenStateRepository: ScreenStateRepository,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = _state.asStateFlow()

    init {
        observeOrientation()
        observeSettings()
        observeDndState()
        observeScreenState()
        screenStateRepository.startMonitoring()
        updateServiceState()
    }

    private fun updateServiceState() {
        val isRunning = isFlipDetectorServiceRunning()
        _state.update { it.copy(isServiceRunning = isRunning) }
    }

    private fun isFlipDetectorServiceRunning(): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Integer.MAX_VALUE)
            .any { it.service.className == FlipDetectorService::class.java.name }
    }

    fun toggleService() {
        val serviceIntent = Intent(context, FlipDetectorService::class.java)
        if (isFlipDetectorServiceRunning()) {
            context.stopService(serviceIntent)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
        updateServiceState()
    }

    private fun observeScreenState() {
        viewModelScope.launch {
            screenStateRepository.isScreenOff().collect { screenOff ->
                Log.d("MainViewModel", "Screen state changed: ${if (screenOff) "OFF" else "ON"}")
            }
        }
    }

    private fun observeOrientation() {
        viewModelScope.launch {
            getOrientationUseCase().collect { orientation ->
                Log.d("MainViewModel", "New orientation: $orientation")
                _state.update { it.copy(orientation = orientation) }
            }
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            getSettingsUseCase.getScreenOffOnlyEnabled().collect { enabled ->
                _state.update { it.copy(isScreenOffOnly = enabled) }
            }
        }
        viewModelScope.launch {
            getSettingsUseCase.getVibrationEnabled().collect { enabled ->
                _state.update { it.copy(isVibrationEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            getSettingsUseCase.getSoundEnabled().collect { enabled ->
                _state.update { it.copy(isSoundEnabled = enabled) }
            }
        }
    }

    private fun observeDndState() {
        viewModelScope.launch {
            // Combine DND enabled state, activation mode, and specific modes
            combine(
                dndRepository.isActivated(),
                settingsRepository.getActivationMode(),
                settingsRepository.getDndMode(),
                settingsRepository.getRingerMode()
            ) { isActivated, activationMode, dndMode, ringerMode ->
                val modeResId = if (activationMode == dev.robin.flip_2_dnd.core.ActivationMode.DND) {
                    when (dndMode) {
                        dev.robin.flip_2_dnd.core.DndMode.PRIORITY -> R.string.dnd_mode_priority
                        dev.robin.flip_2_dnd.core.DndMode.TOTAL_SILENCE -> R.string.dnd_mode_total_silence
                        dev.robin.flip_2_dnd.core.DndMode.ALARMS_ONLY -> R.string.dnd_mode_alarms_only
                    }
                } else {
                    when (ringerMode) {
                        dev.robin.flip_2_dnd.core.RingerMode.SILENT -> R.string.status_ringer_silent
                        dev.robin.flip_2_dnd.core.RingerMode.VIBRATE -> R.string.status_ringer_vibrate
                        dev.robin.flip_2_dnd.core.RingerMode.NORMAL -> R.string.dnd_mode_all
                    }
                }

                _state.update {
                    it.copy(
                        isDndEnabled = isActivated,
                        dndMode = modeResId
                    )
                }
            }.collect()
        }
    }

    override fun onCleared() {
        super.onCleared()
        screenStateRepository.stopMonitoring()
    }
}
