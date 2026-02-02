package dev.robin.flip_2_dnd.data.repository

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.robin.flip_2_dnd.domain.repository.DndRepository
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
	private val settingsRepository: SettingsRepository
) : DndRepository {
	private val notificationManager =
		context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
	private val _isDndEnabled = MutableStateFlow(false)
	private val _dndMode = MutableStateFlow("All Notifications")

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

	override fun isDndEnabled(): Flow<Boolean> = _isDndEnabled
	override fun getDndMode(): Flow<String> = _dndMode

	override suspend fun setDndEnabled(enabled: Boolean) {
		if (!notificationManager.isNotificationPolicyAccessGranted) {
			Log.e("DndRepository", "No notification policy access granted")
			return
		}

		try {
			val newFilter = if (enabled) {
				val isPriorityDndEnabled = runBlocking {
					settingsRepository.getPriorityDndEnabled().first()
				}
				if (isPriorityDndEnabled) {
					NotificationManager.INTERRUPTION_FILTER_PRIORITY
				} else {
					NotificationManager.INTERRUPTION_FILTER_NONE
				}
			} else {
				NotificationManager.INTERRUPTION_FILTER_ALL
			}
			notificationManager.setInterruptionFilter(newFilter)
			updateDndState() // Ensure state is updated immediately after changing
			Log.d("DndRepository", "DND state changed to: $enabled")
		} catch (e: Exception) {
			Log.e("DndRepository", "Error setting DND state", e)
		}
	}

	override suspend fun toggleDnd() {
		val currentState = _isDndEnabled.value
		setDndEnabled(!currentState)
	}

	private fun updateDndState() {
		// Use Android's native method to check if DND is active
		val currentFilter = notificationManager.currentInterruptionFilter

		val isDndActive = currentFilter != NotificationManager.INTERRUPTION_FILTER_ALL

		val dndMode = when (currentFilter) {
			NotificationManager.INTERRUPTION_FILTER_NONE -> "Total Silence"
			NotificationManager.INTERRUPTION_FILTER_PRIORITY -> "Priority Mode"
			NotificationManager.INTERRUPTION_FILTER_ALARMS -> "Alarms Only"
			NotificationManager.INTERRUPTION_FILTER_ALL -> "All Notifications"
			else -> "Unknown Mode"
		}

		// Only update if the state has changed to avoid unnecessary updates
		if (_isDndEnabled.value != isDndActive) {
			_isDndEnabled.value = isDndActive
		}

		if (_dndMode.value != dndMode) {
			_dndMode.value = dndMode
		}

//        Log.d("DndRepository", """
//            Current DND Details:
//            - Active: $isDndActive
//            - Mode: $dndMode
//            - Raw Filter: $currentFilter
//        """.trimIndent())
	}

	// Ensure the monitoring job is cancelled when the repository is no longer used
	override fun onCleared() {
		dndStateUpdateJob.cancel()
	}
}
