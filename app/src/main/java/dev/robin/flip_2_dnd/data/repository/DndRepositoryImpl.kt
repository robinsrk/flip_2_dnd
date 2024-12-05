package dev.robin.flip_2_dnd.data.repository

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import dev.robin.flip_2_dnd.domain.repository.DndRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DndRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : DndRepository {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val _isDndEnabled = MutableStateFlow(false)

    init {
        updateDndState()
    }

    override fun isDndEnabled(): Flow<Boolean> = _isDndEnabled

    override suspend fun setDndEnabled(enabled: Boolean) {
        if (!notificationManager.isNotificationPolicyAccessGranted) {
            Log.e("DndRepository", "No notification policy access granted")
            return
        }

        try {
            val newFilter = if (enabled) {
                NotificationManager.INTERRUPTION_FILTER_NONE
            } else {
                NotificationManager.INTERRUPTION_FILTER_ALL
            }
            notificationManager.setInterruptionFilter(newFilter)
            updateDndState()
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
        val currentFilter = notificationManager.currentInterruptionFilter
        val isDndEnabled = currentFilter == NotificationManager.INTERRUPTION_FILTER_NONE
        _isDndEnabled.value = isDndEnabled
        Log.d("DndRepository", "Current DND state: $isDndEnabled (Filter: $currentFilter)")
    }
}
