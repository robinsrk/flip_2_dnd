package dev.robin.flip_2_dnd.services

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.util.Log
import dev.robin.flip_2_dnd.R
import dev.robin.flip_2_dnd.data.repository.SettingsRepositoryImpl
import dev.robin.flip_2_dnd.domain.repository.SettingsRepository
import dev.robin.flip_2_dnd.presentation.settings.VibrationPattern
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private const val TAG = "DndService"

class DndService(private val context: Context) {
	private val notificationManager =
		context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
	private val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
		val vibratorManager =
			context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
		vibratorManager.defaultVibrator
	} else {
		@Suppress("DEPRECATION")
		context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
	}
	private val settingsRepository: SettingsRepository = SettingsRepositoryImpl(context)
	private val soundService = SoundService(context)

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
			Log.d(
				TAG,
				"Vibration check: enabled=$isVibrationEnabled, pattern=${pattern.contentToString()}"
			)

			if (!isVibrationEnabled) {
				Log.w(TAG, "Vibration is disabled. Skipping vibration.")
				return@runBlocking
			}

			try {
				val useCustomVibration = settingsRepository.getUseCustomVibration().first()
				val customStrength = if (useCustomVibration) {
					settingsRepository.getCustomVibrationStrength().first()
				} else {
					1.0f
				}

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
					val baseAmplitude = (255 * customStrength).toInt().coerceIn(1, 255)
					val amplitudes = IntArray(pattern.size) { index ->
						// Ensure zero amplitude for timing intervals and full amplitude for vibration periods
						if (index % 2 == 0) 0 else baseAmplitude
					}
					
					// Adjust timing to ensure precise vibration pattern
					val adjustedPattern = pattern.map { duration ->
						// Ensure minimum duration for better perception
						duration.coerceAtLeast(50L)
					}.toLongArray()
					
					Log.d(TAG, "Creating waveform vibration with amplitude: $baseAmplitude and pattern: ${adjustedPattern.contentToString()}")
					vibrator.vibrate(VibrationEffect.createWaveform(adjustedPattern, amplitudes, -1))
				} else {
					Log.d(TAG, "Using deprecated vibration method")
					@Suppress("DEPRECATION")
					vibrator.vibrate(pattern, -1)
				}
			} catch (e: Exception) {
				Log.e(TAG, "Error during vibration: ${e.message}", e)
			}
		}
	}

    private fun playSound(isEnabled: Boolean) {
        soundService.playDndSound(isEnabled)
    }

    private fun checkDndPermission(): Boolean {
        val hasPermission = notificationManager.isNotificationPolicyAccessGranted
        Log.d(TAG, "DND Permission check: $hasPermission")
        return hasPermission
    }

    private fun openDndSettings() {
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

            // Determine the new filter first
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

            // Play feedback BEFORE setting the filter
            val willBeDndEnabled = newFilter != NotificationManager.INTERRUPTION_FILTER_ALL
            playSound(willBeDndEnabled)

            // Get the appropriate vibration pattern from settings
            runBlocking {
                val pattern = if (willBeDndEnabled) {
                    settingsRepository.getDndOnVibration().first()
                } else {
                    settingsRepository.getDndOffVibration().first()
                }
                
                // Convert the pattern to timing array
                val timings = when (pattern) {
                    VibrationPattern.SINGLE_PULSE -> pattern.pattern
                    VibrationPattern.DOUBLE_PULSE -> pattern.pattern
                    VibrationPattern.LONG_PULSE -> pattern.pattern
                    VibrationPattern.NONE -> longArrayOf()
                }
                
                if (timings.isNotEmpty()) {
                    vibrate(timings)
                }
            }

            // Add 2-second delay only when turning on Total Silence DND
            if (willBeDndEnabled && newFilter == NotificationManager.INTERRUPTION_FILTER_NONE) {
                runBlocking {
                    Log.d(TAG, "Waiting 2 seconds before setting Total Silence DND")
                    delay(2000)
                }
            }

            // Now set the interruption filter
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
            Log.d(
                TAG,
                "DND ${if (_isDndEnabled.value) "enabled" else "disabled"} by ${if (_isAppEnabledDnd.value) "app" else "user"}"
            )

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
            Log.d(
                TAG,
                "Updated DND status: isDndEnabled=${_isDndEnabled.value}, isAppEnabled=${_isAppEnabledDnd.value} (filter: $currentFilter)"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error updating DND status: ${e.message}", e)
        }
    }
}
