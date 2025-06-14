package dev.robin.dndfy.presentation.main

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.robin.dndfy.domain.model.PhoneOrientation
import dev.robin.dndfy.domain.repository.DndRepository
import dev.robin.dndfy.domain.repository.FeedbackRepository
import dev.robin.dndfy.domain.repository.ScreenStateRepository
import dev.robin.dndfy.domain.usecase.GetOrientationUseCase
import dev.robin.dndfy.domain.usecase.GetSettingsUseCase
import dev.robin.dndfy.domain.usecase.ToggleDndUseCase
import dev.robin.dndfy.services.DNDfyDetectorService
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
    private val feedbackRepository: FeedbackRepository,
    private val screenStateRepository: ScreenStateRepository,
    @ApplicationContext private val context: Context
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
        val isRunning = isDNDfyDetectorServiceRunning()
        _state.update { it.copy(isServiceRunning = isRunning) }
    }

    private fun isDNDfyDetectorServiceRunning(): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Integer.MAX_VALUE)
            .any { it.service.className == DNDfyDetectorService::class.java.name }
    }

    fun toggleService() {
        val serviceIntent = Intent(context, DNDfyDetectorService::class.java)
        if (isDNDfyDetectorServiceRunning()) {
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
            // Combine DND enabled state and mode
            combine(
                dndRepository.isDndEnabled(),
                dndRepository.getDndMode()
            ) { enabled, mode ->
                Log.d("MainViewModel", "DND state changed: $enabled, Mode: $mode")
                _state.update { 
                    it.copy(
                        isDndEnabled = enabled,
                        dndMode = mode
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
