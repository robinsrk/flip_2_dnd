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
import dev.robin.flip_2_dnd.domain.repository.SettingsRepository
import dev.robin.flip_2_dnd.domain.repository.DndRepository
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
	private val settingsRepository: SettingsRepository,
	private val dndRepository: DndRepository
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

	var isFlashlightOn = false
		private set

	private val torchCallback = object : CameraManager.TorchCallback() {
		override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
			super.onTorchModeChanged(cameraId, enabled)
			if (cameraId == this@DndService.cameraId) {
				isFlashlightOn = enabled
				Log.d(TAG, "Flashlight state changed: $isFlashlightOn")
			}
		}
	}

	init {
		// We can still use updateDndStatus for local state if needed, but repository is the source of truth
		updateDndStatus()
		try {
			cameraManager.registerTorchCallback(torchCallback, null)
		} catch (e: Exception) {
			Log.e(TAG, "Error registering torch callback: ${e.message}")
		}
	}

	fun cleanup() {
		try {
			cameraManager.unregisterTorchCallback(torchCallback)
		} catch (e: Exception) {
			Log.e(TAG, "Error unregistering torch callback: ${e.message}")
		}
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
        try {
            val isCurrentlyActivated = runBlocking { dndRepository.isActivated().first() }
            val willBeActivated = !isCurrentlyActivated

            // Play feedback BEFORE setting the filter/ringer
            playSound(willBeActivated)

            // Get the appropriate vibration pattern from settings
            runBlocking {
                val pattern = if (willBeActivated) {
                    settingsRepository.getDndOnVibration().first()
                } else {
                    settingsRepository.getDndOffVibration().first()
                }
                
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
                val flashlightPattern = if (willBeActivated) {
                    settingsRepository.getDndOnFlashlightPattern().first()
                } else {
                    settingsRepository.getDndOffFlashlightPattern().first()
                }
                flashFlashlight(flashlightPattern)
            }

            // Handle delay for Total Silence DND if applicable
            val activationMode = runBlocking { settingsRepository.getActivationMode().first() }
            val dndMode = runBlocking { settingsRepository.getDndMode().first() }
            
            if (willBeActivated && 
                activationMode == dev.robin.flip_2_dnd.domain.repository.ActivationMode.DND && 
                dndMode == dev.robin.flip_2_dnd.domain.repository.DndMode.TOTAL_SILENCE) {
                runBlocking {
                    Log.d(TAG, "Waiting 2 seconds before setting Total Silence DND")
                    delay(2000)
                }
            }

            // Now set the activation state in the repository
            runBlocking {
                dndRepository.setActivated(willBeActivated)
            }

            // Update local state flows for UI sync
            _isDndEnabled.value = willBeActivated
            _isAppEnabledDnd.value = willBeActivated

        } catch (e: Exception) {
            Log.e(TAG, "Error toggling activation state: ${e.message}", e)
        }
    }

    fun updateDndStatus() {
        try {
            val activationMode = runBlocking { settingsRepository.getActivationMode().first() }
            val isActivated = runBlocking { dndRepository.isActivated().first() }
            
            if (activationMode == dev.robin.flip_2_dnd.domain.repository.ActivationMode.DND) {
                val currentFilter = notificationManager.currentInterruptionFilter
                val isDndOn = currentFilter != NotificationManager.INTERRUPTION_FILTER_ALL
                _isDndEnabled.value = isDndOn
                if (!isDndOn) {
                    _isAppEnabledDnd.value = false
                }
            } else {
                // In Ringer mode, we rely on the repository's activation state
                _isDndEnabled.value = isActivated
                if (!isActivated) {
                    _isAppEnabledDnd.value = false
                }
            }
            
            Log.d(
                TAG,
                "Updated status: isActivated=$isActivated, mode=$activationMode, isDndEnabled=${_isDndEnabled.value}, isAppEnabled=${_isAppEnabledDnd.value}"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error updating status: ${e.message}", e)
        }
    }

	private fun flashFlashlight(pattern: FlashlightPattern) {
		if (cameraId == null || pattern == FlashlightPattern.NONE) return

		runBlocking {
			val isFlashlightEnabled = settingsRepository.getFlashlightFeedbackEnabled().first()
			if (!isFlashlightEnabled) return@runBlocking

			// Check if Pro feature
			if (!dev.robin.flip_2_dnd.PremiumProvider.engine.flashlightFeedbackEnabled()) return@runBlocking

			val feedbackWithFlashlightOn = settingsRepository.getFeedbackWithFlashlightOn().first()

			// Logic for existing flashlight state
			if (isFlashlightOn) {
				if (!feedbackWithFlashlightOn) {
					Log.d(TAG, "Flashlight is already ON and feedbackWithFlashlightOn is FALSE - Skipping feedback")
					return@runBlocking
				}
				// If we proceed, we must restore to ON
			}

			// Check schedule
			val scheduleEnabled = settingsRepository.getFlashlightScheduleEnabled().first()
			if (scheduleEnabled) {
				val startTime = settingsRepository.getFlashlightScheduleStartTime().first()
				val endTime = settingsRepository.getFlashlightScheduleEndTime().first()
				val days = settingsRepository.getFlashlightScheduleDays().first()
				if (!isWithinSchedule(startTime, endTime, days)) {
					Log.d(TAG, "Current time is outside flashlight schedule. Skipping flashlight blink.")
					return@runBlocking
				}
			}

			try {
				// We rely on the tracked 'isFlashlightOn' state for restoration
				val originalState = isFlashlightOn
				
				pattern.pattern.forEachIndexed { index, duration ->
					if (index == 0) {
						if (duration > 0) delay(duration)
					} else {
						val turnOn = index % 2 != 0
						cameraManager.setTorchMode(cameraId, turnOn)
						delay(duration)
					}
				}
				
				// Restore original state
				Log.d(TAG, "Restoring flashlight state to: $originalState")
				cameraManager.setTorchMode(cameraId, originalState)
				
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
