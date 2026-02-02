package dev.robin.flip_2_dnd.services

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
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
import dev.robin.flip_2_dnd.presentation.settings.FlashlightPattern
import dev.robin.flip_2_dnd.presentation.settings.VibrationPattern
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private const val TAG = "DndService"

class DndService(
	private val context: Context,
	private val settingsRepository: SettingsRepository
) {
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
	private val soundService = SoundService(context, settingsRepository)

	private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
	private val cameraId = try {
		cameraManager.cameraIdList.firstOrNull { id ->
			val characteristics = cameraManager.getCameraCharacteristics(id)
			characteristics.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
		}
	} catch (e: Exception) {
		null
	}

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

			// Check vibration schedule
			val scheduleEnabled = settingsRepository.getVibrationScheduleEnabled().first()
			if (scheduleEnabled) {
				val startTime = settingsRepository.getVibrationScheduleStartTime().first()
				val endTime = settingsRepository.getVibrationScheduleEndTime().first()
				val days = settingsRepository.getVibrationScheduleDays().first()
				if (!isWithinSchedule(startTime, endTime, days)) {
					Log.d(TAG, "Current time is outside vibration schedule. Skipping vibration.")
					return@runBlocking
				}
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
        runBlocking {
            val scheduleEnabled = settingsRepository.getSoundScheduleEnabled().first()
            if (scheduleEnabled) {
                val startTime = settingsRepository.getSoundScheduleStartTime().first()
                val endTime = settingsRepository.getSoundScheduleEndTime().first()
                val days = settingsRepository.getSoundScheduleDays().first()
                if (!isWithinSchedule(startTime, endTime, days)) {
                    Log.d(TAG, "Current time is outside sound schedule. Skipping sound.")
                    return@runBlocking
                }
            }
            soundService.playDndSound(isEnabled)
        }
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
                val timings = if (pattern == VibrationPattern.NONE) {
                    longArrayOf()
                } else {
                    pattern.pattern
                }
                
                if (timings.isNotEmpty()) {
                    vibrate(timings)
                }
            }

            // Get the appropriate flashlight pattern from settings
            runBlocking {
                val flashlightPattern = if (willBeDndEnabled) {
                    settingsRepository.getDndOnFlashlightPattern().first()
                } else {
                    settingsRepository.getDndOffFlashlightPattern().first()
                }
                flashFlashlight(flashlightPattern)
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

    private fun flashFlashlight(pattern: FlashlightPattern) {
		if (cameraId == null || pattern == FlashlightPattern.NONE) return

		runBlocking {
			val isFlashlightEnabled = settingsRepository.getFlashlightFeedbackEnabled().first()
			if (!isFlashlightEnabled) return@runBlocking

			// Check if Pro feature
			if (!dev.robin.flip_2_dnd.PremiumProvider.engine.flashlightFeedbackEnabled()) return@runBlocking

			try {
				pattern.pattern.forEachIndexed { index, duration ->
					if (index == 0) {
						if (duration > 0) delay(duration)
					} else {
						val isOn = index % 2 != 0
						cameraManager.setTorchMode(cameraId, isOn)
						delay(duration)
					}
				}
				// Ensure it's off at the end
				cameraManager.setTorchMode(cameraId, false)
			} catch (e: Exception) {
				Log.e(TAG, "Error flashing flashlight: ${e.message}")
			}
		}
	}

	private fun isWithinSchedule(startTime: String, endTime: String, days: Set<Int>): Boolean {
        try {
            val now = java.util.Calendar.getInstance()
            val dayOfWeek = now.get(java.util.Calendar.DAY_OF_WEEK)
            
            if (!days.contains(dayOfWeek)) {
                return false
            }

            val currentTime = String.format("%02d:%02d", 
                now.get(java.util.Calendar.HOUR_OF_DAY), 
                now.get(java.util.Calendar.MINUTE))
            
            return if (startTime <= endTime) {
                currentTime in startTime..endTime
            } else {
                // Overnight schedule
                currentTime >= startTime || currentTime <= endTime
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking schedule: ${e.message}")
            return true
        }
    }
}
