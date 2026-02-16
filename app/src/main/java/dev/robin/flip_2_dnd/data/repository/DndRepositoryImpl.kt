package dev.robin.flip_2_dnd.data.repository

import android.app.NotificationManager
import android.media.AudioManager
import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.robin.flip_2_dnd.R
import dev.robin.flip_2_dnd.domain.repository.DndRepository
import dev.robin.flip_2_dnd.domain.repository.HistoryRepository
import dev.robin.flip_2_dnd.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DndRepositoryImpl @Inject constructor(
	@param:ApplicationContext private val context: Context,
	private val settingsRepository: SettingsRepository,
	private val historyRepository: HistoryRepository
) : DndRepository {
	private val notificationManager =
		context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
	private val audioManager =
		context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
		
	private val _isActivated = MutableStateFlow(false)
	private val _dndMode = MutableStateFlow(R.string.dnd_mode_all)
	
	private val dndStateUpdateJob: Job

	init {
		updateDndState()
		// Start continuous monitoring of DND state
		dndStateUpdateJob = startDndStateMonitoring()
	}

	private fun startDndStateMonitoring(): Job = CoroutineScope(Dispatchers.Default).launch {
		while (isActive) {
			updateDndState()
			delay(1000) // Check every second
		}
	}

	override fun isActivated(): Flow<Boolean> = _isActivated
	override fun getDndMode(): Flow<Int> = _dndMode

	override suspend fun setActivated(enabled: Boolean) {
		if (enabled == _isActivated.value) return

		// Update state immediately to prevent race conditions or duplicate triggers
		_isActivated.value = enabled
		
		val activationMode = settingsRepository.getActivationMode().first()
		
		if (activationMode == dev.robin.flip_2_dnd.domain.repository.ActivationMode.DND) {
			if (!notificationManager.isNotificationPolicyAccessGranted) {
				Log.e("DndRepository", "No notification policy access granted")
				_isActivated.value = false // Revert state
				return
			}
			try {
				val newFilter = if (enabled) {
					settingsRepository.getDndMode().first().filter
				} else {
					NotificationManager.INTERRUPTION_FILTER_ALL
				}
				notificationManager.setInterruptionFilter(newFilter)
				Log.d("DndRepository", "DND state changed to: $enabled with filter $newFilter")
			} catch (e: Exception) {
				Log.e("DndRepository", "Error setting DND state", e)
			}
		} else {
			// RINGER mode
			try {
				if (enabled) {
					// Save current mode before switching
					val currentMode = audioManager.ringerMode
					settingsRepository.setPreviousRingerMode(currentMode)
					
					val targetMode = settingsRepository.getRingerMode().first().value
					audioManager.ringerMode = targetMode
					Log.d("DndRepository", "Ringer mode changed from $currentMode to $targetMode")
				} else {
					// Restore previous mode
					val restoredMode = settingsRepository.getPreviousRingerMode().first()
					audioManager.ringerMode = restoredMode
					Log.d("DndRepository", "Ringer mode restored to: $restoredMode")
				}
			} catch (e: Exception) {
				Log.e("DndRepository", "Error setting Ringer mode", e)
			}
		}
		
		updateDndState()
	}

	override suspend fun toggle() {
		val currentState = _isActivated.value
		setActivated(!currentState)
	}

	private fun updateDndState() {
		val currentFilter = notificationManager.currentInterruptionFilter
		val isDndActive = currentFilter != NotificationManager.INTERRUPTION_FILTER_ALL
		
		val activationMode = runBlocking { settingsRepository.getActivationMode().first() }

		val dndModeText = if (activationMode == dev.robin.flip_2_dnd.domain.repository.ActivationMode.DND) {
			when (currentFilter) {
				NotificationManager.INTERRUPTION_FILTER_NONE -> R.string.dnd_mode_total_silence
				NotificationManager.INTERRUPTION_FILTER_PRIORITY -> R.string.dnd_mode_priority
				NotificationManager.INTERRUPTION_FILTER_ALARMS -> R.string.dnd_mode_alarms_only
				NotificationManager.INTERRUPTION_FILTER_ALL -> R.string.dnd_mode_all
				else -> R.string.dnd_mode_unknown
			}
		} else {
			if (_isActivated.value) {
				val selectedRinger = runBlocking { settingsRepository.getRingerMode().first() }
				when (selectedRinger) {
					dev.robin.flip_2_dnd.domain.repository.RingerMode.SILENT -> R.string.status_ringer_silent
					dev.robin.flip_2_dnd.domain.repository.RingerMode.VIBRATE -> R.string.status_ringer_vibrate
					dev.robin.flip_2_dnd.domain.repository.RingerMode.NORMAL -> R.string.dnd_mode_all
				}
			} else {
				R.string.dnd_mode_all
			}
		}

		if (_dndMode.value != dndModeText) {
			_dndMode.value = dndModeText
		}
		
		// Synchronization logic
		if (activationMode == dev.robin.flip_2_dnd.domain.repository.ActivationMode.DND) {
			// In DND mode, we sync with system DND state
			if (isDndActive && !_isActivated.value) {
				_isActivated.value = true
				CoroutineScope(Dispatchers.IO).launch {
					historyRepository.addHistory(true, currentFilter)
				}
			} else if (!isDndActive && _isActivated.value) {
				_isActivated.value = false
				CoroutineScope(Dispatchers.IO).launch {
					historyRepository.addHistory(false, currentFilter)
				}
			}
		} else {
			// In RINGER mode, we don't sync with system DND state because 
			// setting ringer to silent might trigger DND on some devices, 
			// which would mess up our "isActivated" if we synced back.
			// We only record history when state changes via setActivated.
		}
	}

//        Log.d("DndRepository", """
//            Current DND Details:
//            - Active: $isDndActive
//            - Mode: $dndMode
//            - Raw Filter: $currentFilter
//        """.trimIndent())

	// Ensure the monitoring job is cancelled when the repository is no longer used
	override fun onCleared() {
		dndStateUpdateJob.cancel()
	}
}
