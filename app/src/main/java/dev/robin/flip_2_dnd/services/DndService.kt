package dev.robin.flip_2_dnd.services

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.*
import android.provider.Settings
import android.util.Log
import dev.robin.flip_2_dnd.R
import dev.robin.flip_2_dnd.domain.repository.SettingsRepository
import dev.robin.flip_2_dnd.data.repository.SettingsRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

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
    private val settingsRepository: SettingsRepository = SettingsRepositoryImpl(context)
    private var mediaPlayer: MediaPlayer? = null
    
    private val _isDndEnabled = MutableStateFlow(false)
    val isDndEnabled: StateFlow<Boolean> = _isDndEnabled
    
    private val _isAppEnabledDnd = MutableStateFlow(false)
    val isAppEnabledDnd: StateFlow<Boolean> = _isAppEnabledDnd

    init {
        updateDndStatus()
    }

    private fun vibrate(pattern: LongArray) {
        runBlocking {
            val isVibrationEnabled = settingsRepository.getVibrationEnabled().first()
            Log.d(TAG, "Vibration check: enabled=$isVibrationEnabled")
            if (isVibrationEnabled) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val timings = pattern
                    val amplitudes = IntArray(pattern.size) { VibrationEffect.DEFAULT_AMPLITUDE }
                    vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(pattern, -1)
                }
            }
        }
    }

    private fun playSound(isEnabled: Boolean) {
        runBlocking {
            val isSoundEnabled = settingsRepository.getSoundEnabled().first()
            Log.d(TAG, "Sound check: enabled=$isSoundEnabled, DND state=$isEnabled")
            if (isSoundEnabled) {
                try {
                    mediaPlayer?.release()
                    val resourceId = if (isEnabled) R.raw.shh else R.raw.vivo_whistle
                    mediaPlayer = MediaPlayer.create(context, resourceId)
                    mediaPlayer?.setOnCompletionListener { it.release() }
                    mediaPlayer?.start()
                } catch (e: Exception) {
                    Log.e(TAG, "Error playing sound: ${e.message}", e)
                }
            }
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
                // If DND is currently OFF, turn it ON with appropriate mode
                runBlocking {
                    if (settingsRepository.getPriorityDndEnabled().first()) {
                        Log.d(TAG, "Using Priority DND mode")
                        NotificationManager.INTERRUPTION_FILTER_PRIORITY
                    } else {
                        Log.d(TAG, "Using Total Silence DND mode")
                        NotificationManager.INTERRUPTION_FILTER_NONE
                    }
                }
            } else {
                // If DND is currently ON (any mode), turn it OFF
                NotificationManager.INTERRUPTION_FILTER_ALL
            }
            
            Log.d(TAG, "Setting DND filter from $currentFilter to $newFilter")
            notificationManager.setInterruptionFilter(newFilter)
            
            // Update our state flow
            _isDndEnabled.value = newFilter != NotificationManager.INTERRUPTION_FILTER_ALL
            Log.d(TAG, "Previous app-enabled state: ${_isAppEnabledDnd.value}")
            if (newFilter != NotificationManager.INTERRUPTION_FILTER_ALL) {
                _isAppEnabledDnd.value = true
                Log.d(TAG, "Setting app-enabled DND to true")
            } else {
                _isAppEnabledDnd.value = false
                Log.d(TAG, "Setting app-enabled DND to false")
            }
            Log.d(TAG, "DND ${if (_isDndEnabled.value) "enabled" else "disabled"} by ${if (_isAppEnabledDnd.value) "app" else "user"}")
            
            // Play feedback
            playSound(_isDndEnabled.value)
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
        try {
            val currentFilter = notificationManager.currentInterruptionFilter
            val isDndOn = currentFilter != NotificationManager.INTERRUPTION_FILTER_ALL
            _isDndEnabled.value = isDndOn
            // Only reset app-enabled state if DND is turned off
            if (!isDndOn) {
                _isAppEnabledDnd.value = false
            }
            Log.d(TAG, "Updated DND status: isDndEnabled=${_isDndEnabled.value}, isAppEnabled=${_isAppEnabledDnd.value} (filter: $currentFilter)")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating DND status: ${e.message}", e)
        }
    }
}
