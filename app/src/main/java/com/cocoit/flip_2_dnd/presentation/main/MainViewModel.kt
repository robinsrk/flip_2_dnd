package com.cocoit.flip_2_dnd.presentation.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocoit.flip_2_dnd.domain.model.PhoneOrientation
import com.cocoit.flip_2_dnd.domain.repository.DndRepository
import com.cocoit.flip_2_dnd.domain.repository.FeedbackRepository
import com.cocoit.flip_2_dnd.domain.repository.ScreenStateRepository
import com.cocoit.flip_2_dnd.domain.usecase.GetOrientationUseCase
import com.cocoit.flip_2_dnd.domain.usecase.GetSettingsUseCase
import com.cocoit.flip_2_dnd.domain.usecase.ToggleDndUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val screenStateRepository: ScreenStateRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = _state.asStateFlow()

    private var lastOrientation = PhoneOrientation.UNKNOWN
    private var faceDownJob: Job? = null
    private var isScreenOff = false

    init {
        observeOrientation()
        observeSettings()
        observeDndState()
        observeScreenState()
        screenStateRepository.startMonitoring()
    }

    private fun observeScreenState() {
        viewModelScope.launch {
            screenStateRepository.isScreenOff().collect { screenOff ->
                isScreenOff = screenOff
                Log.d("MainViewModel", "Screen state changed: ${if (screenOff) "OFF" else "ON"}")
            }
        }
    }

    private fun observeOrientation() {
        viewModelScope.launch {
            getOrientationUseCase().collect { orientation ->
                Log.d("MainViewModel", "New orientation: $orientation (Last: $lastOrientation)")
                _state.update { it.copy(orientation = orientation) }
                
                // Handle orientation changes
                if (orientation != lastOrientation) {
                    when (orientation) {
                        PhoneOrientation.FACE_DOWN -> {
                            if (!state.value.isDndEnabled) {
                                // Check if we should process this change
                                if (!state.value.isScreenOffOnly || isScreenOff) {
                                    // Cancel any existing job
                                    faceDownJob?.cancel()
                                    // Start a new delayed job
                                    faceDownJob = viewModelScope.launch {
                                        delay(2000) // 2 seconds delay
                                        if (state.value.orientation == PhoneOrientation.FACE_DOWN) {
                                            Log.d("MainViewModel", "Phone face down for 2 seconds, enabling DND")
                                            toggleDndUseCase()
                                            provideFeedback(true)
                                        }
                                    }
                                } else {
                                    Log.d("MainViewModel", "Ignoring face down - screen is on and 'Only when screen off' is enabled")
                                }
                            }
                        }
                        PhoneOrientation.FACE_UP -> {
                            // Cancel any pending face down job
                            faceDownJob?.cancel()
                            faceDownJob = null
                            
                            if (state.value.isDndEnabled) {
                                // Check if we should process this change
                                if (!state.value.isScreenOffOnly || isScreenOff) {
                                    Log.d("MainViewModel", "Phone face up, disabling DND")
                                    toggleDndUseCase()
                                    provideFeedback(false)
                                } else {
                                    Log.d("MainViewModel", "Ignoring face up - screen is on and 'Only when screen off' is enabled")
                                }
                            }
                        }
                        PhoneOrientation.UNKNOWN -> {
                            // Cancel any pending face down job
                            faceDownJob?.cancel()
                            faceDownJob = null
                            Log.d("MainViewModel", "Phone orientation unknown")
                        }
                    }
                    lastOrientation = orientation
                }
            }
        }
    }

    private fun provideFeedback(dndEnabled: Boolean) {
        if (state.value.isVibrationEnabled) {
            feedbackRepository.vibrate()
        }
        if (state.value.isSoundEnabled && !dndEnabled) { // Only play sound when DND is being disabled
            feedbackRepository.playSound()
        }
    }

    private fun observeDndState() {
        viewModelScope.launch {
            dndRepository.isDndEnabled().collect { enabled ->
                Log.d("MainViewModel", "DND state changed: $enabled")
                _state.update { it.copy(isDndEnabled = enabled) }
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

    override fun onCleared() {
        super.onCleared()
        faceDownJob?.cancel()
        screenStateRepository.stopMonitoring()
    }
}
