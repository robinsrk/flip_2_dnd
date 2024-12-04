package com.cocoit.flip_2_dnd.services

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.*
import android.provider.Settings
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

private const val TAG = "DndService"

class DndService(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    
    private val _isDndEnabled = MutableStateFlow(false)
    val isDndEnabled: StateFlow<Boolean> = _isDndEnabled

    init {
        updateDndStatus()
    }

    private fun vibrate(pattern: LongArray) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = pattern
            val amplitudes = IntArray(pattern.size) { VibrationEffect.DEFAULT_AMPLITUDE }
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }

    fun checkDndPermission(): Boolean {
        val hasPermission = notificationManager.isNotificationPolicyAccessGranted
        Log.d(TAG, "DND Permission check: $hasPermission")
        return hasPermission
    }

    fun openDndSettings() {
        Log.d(TAG, "Opening DND settings")
        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun toggleDnd() {
        if (!checkDndPermission()) {
            Log.e(TAG, "No DND permission, opening settings")
            openDndSettings()
            return
        }

        try {
            val currentFilter = notificationManager.currentInterruptionFilter
            Log.d(TAG, "Current DND filter: $currentFilter")
            
            // INTERRUPTION_FILTER_ALL means DND is OFF
            // Any other value means DND is ON with different levels
            val isDndCurrentlyOn = currentFilter != NotificationManager.INTERRUPTION_FILTER_ALL
            
            val newFilter = if (!isDndCurrentlyOn) {
                // If DND is currently OFF, turn it ON with PRIORITY mode
                NotificationManager.INTERRUPTION_FILTER_PRIORITY
            } else {
                // If DND is currently ON (any mode), turn it OFF
                NotificationManager.INTERRUPTION_FILTER_ALL
            }
            
            Log.d(TAG, "Setting DND filter from $currentFilter to $newFilter")
            notificationManager.setInterruptionFilter(newFilter)
            
            // Update our state flow
            _isDndEnabled.value = newFilter != NotificationManager.INTERRUPTION_FILTER_ALL
            Log.d(TAG, "DND ${if (_isDndEnabled.value) "enabled" else "disabled"}")
            
            // Vibrate based on the new state
            if (_isDndEnabled.value) {
                // Two vibrations for DND ON
                vibrate(longArrayOf(0, 200, 200, 200))
            } else {
                // One vibration for DND OFF
                vibrate(longArrayOf(0, 200))
            }
            
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException toggling DND: ${e.message}", e)
            openDndSettings()
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling DND: ${e.message}", e)
        }
    }

    fun updateDndStatus() {
        if (!checkDndPermission()) {
            Log.d(TAG, "No DND permission for status update")
            return
        }
        try {
            val currentFilter = notificationManager.currentInterruptionFilter
            val isDndOn = currentFilter != NotificationManager.INTERRUPTION_FILTER_ALL
            _isDndEnabled.value = isDndOn
            Log.d(TAG, "Updated DND status: ${_isDndEnabled.value} (filter: $currentFilter)")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating DND status: ${e.message}", e)
        }
    }
}
