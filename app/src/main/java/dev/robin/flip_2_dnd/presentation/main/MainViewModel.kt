package dev.robin.flip_2_dnd.presentation.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.robin.flip_2_dnd.domain.model.PhoneOrientation
import dev.robin.flip_2_dnd.domain.repository.DndRepository
import dev.robin.flip_2_dnd.domain.repository.FeedbackRepository
import dev.robin.flip_2_dnd.domain.repository.ScreenStateRepository
import dev.robin.flip_2_dnd.domain.usecase.GetOrientationUseCase
import dev.robin.flip_2_dnd.domain.usecase.GetSettingsUseCase
import dev.robin.flip_2_dnd.domain.usecase.ToggleDndUseCase
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
            dndRepository.isDndEnabled().collect { enabled ->
                Log.d("MainViewModel", "DND state changed: $enabled")
                _state.update { it.copy(isDndEnabled = enabled) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        screenStateRepository.stopMonitoring()
    }
}
