package dev.robin.flip_2_dnd.presentation.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.robin.flip_2_dnd.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
	application: Application,
	private val settingsRepository: SettingsRepository
) : AndroidViewModel(application) {

	private val _screenOffOnly = MutableStateFlow(false)
	val screenOffOnly = _screenOffOnly.asStateFlow()

	private val _soundEnabled = MutableStateFlow(true)
	val soundEnabled = _soundEnabled.asStateFlow()

	private val _vibrationEnabled = MutableStateFlow(true)
	val vibrationEnabled = _vibrationEnabled.asStateFlow()

	private val _priorityDndEnabled = MutableStateFlow(false)
	val priorityDndEnabled = _priorityDndEnabled.asStateFlow()
	
	private val _notificationsEnabled = MutableStateFlow(true)
	val notificationsEnabled = _notificationsEnabled.asStateFlow()

	private val _highSensitivityModeEnabled = MutableStateFlow(false)
	val highSensitivityModeEnabled = _highSensitivityModeEnabled.asStateFlow()

	private val _batterySaverOnFlipEnabled = MutableStateFlow(false)
	val batterySaverOnFlipEnabled = _batterySaverOnFlipEnabled.asStateFlow()

	private val _activationDelay = MutableStateFlow(2)
	val activationDelay = _activationDelay.asStateFlow()

	private val _dndOnSound = MutableStateFlow(Sound.SLUSH)
	val dndOnSound = _dndOnSound.asStateFlow()

	private val _dndOffSound = MutableStateFlow(Sound.WHISTLE)
	val dndOffSound = _dndOffSound.asStateFlow()

	private val _useCustomVolume = MutableStateFlow(false)
	val useCustomVolume = _useCustomVolume.asStateFlow()

	private val _customVolume = MutableStateFlow(0.5f)
	val customVolume = _customVolume.asStateFlow()

	val availableSounds = Sound.entries
	val availableVibrationPatterns = VibrationPattern.entries
	private val _useCustomVibration = MutableStateFlow(false)
	val useCustomVibration = _useCustomVibration.asStateFlow()

	private val _customVibrationStrength = MutableStateFlow(1f)
	val customVibrationStrength = _customVibrationStrength.asStateFlow()

	private val _dndOnVibration = MutableStateFlow(VibrationPattern.DOUBLE_PULSE)
	val dndOnVibration = _dndOnVibration.asStateFlow()

	private val _dndOffVibration = MutableStateFlow(VibrationPattern.SINGLE_PULSE)
	val dndOffVibration = _dndOffVibration.asStateFlow()

	private val _flipSensitivity = MutableStateFlow(1f)
	val flipSensitivity = _flipSensitivity.asStateFlow()

	private val _hasSecureSettingsPermission = MutableStateFlow(false)
	val hasSecureSettingsPermission = _hasSecureSettingsPermission.asStateFlow()

	private val _dndOnCustomSoundUri = MutableStateFlow<String?>(null)
	val dndOnCustomSoundUri = _dndOnCustomSoundUri.asStateFlow()

	private val _dndOffCustomSoundUri = MutableStateFlow<String?>(null)
	val dndOffCustomSoundUri = _dndOffCustomSoundUri.asStateFlow()

	init {
		checkSecureSettingsPermission()
		viewModelScope.launch {
			settingsRepository.getScreenOffOnlyEnabled().collect { enabled ->
				_screenOffOnly.value = enabled
			}
		}
		viewModelScope.launch {
			settingsRepository.getSoundEnabled().collect { enabled ->
				_soundEnabled.value = enabled
			}
		}
		viewModelScope.launch {
			settingsRepository.getVibrationEnabled().collect { enabled ->
				_vibrationEnabled.value = enabled
			}
		}
		viewModelScope.launch {
			settingsRepository.getPriorityDndEnabled().collect { enabled ->
				_priorityDndEnabled.value = enabled
			}
		}
		viewModelScope.launch {
			settingsRepository.getNotificationsEnabled().collect { enabled ->
				_notificationsEnabled.value = enabled
			}
		}
		viewModelScope.launch {
			settingsRepository.getHighSensitivityModeEnabled().collect { enabled ->
				_highSensitivityModeEnabled.value = enabled
			}
		}
		viewModelScope.launch {
			settingsRepository.getBatterySaverOnFlipEnabled().collect { enabled ->
				_batterySaverOnFlipEnabled.value = enabled
			}
		}
		viewModelScope.launch {
			settingsRepository.getActivationDelay().collect { delay ->
				_activationDelay.value = delay
			}
		}
		viewModelScope.launch {
			settingsRepository.getDndOnSound().collect { sound ->
				_dndOnSound.value = sound
			}
		}
		viewModelScope.launch {
			settingsRepository.getDndOffSound().collect { sound ->
				_dndOffSound.value = sound
			}
		}
		viewModelScope.launch {
			settingsRepository.getUseCustomVolume().collect { enabled ->
				_useCustomVolume.value = enabled
			}
		}
		viewModelScope.launch {
			settingsRepository.getCustomVolume().collect { volume ->
				_customVolume.value = volume
			}
		}
		viewModelScope.launch {
			settingsRepository.getDndOnCustomSoundUri().collect { uri ->
				_dndOnCustomSoundUri.value = uri
			}
		}
		viewModelScope.launch {
			settingsRepository.getDndOffCustomSoundUri().collect { uri ->
				_dndOffCustomSoundUri.value = uri
			}
		}
		viewModelScope.launch {
			settingsRepository.getUseCustomVibration().collect { enabled ->
				_useCustomVibration.value = enabled
			}
		}
		viewModelScope.launch {
			settingsRepository.getCustomVibrationStrength().collect { strength ->
				_customVibrationStrength.value = strength
			}
		}
		viewModelScope.launch {
			settingsRepository.getDndOnVibration().collect { pattern ->
				_dndOnVibration.value = pattern
			}
		}
		viewModelScope.launch {
			settingsRepository.getDndOffVibration().collect { pattern ->
				_dndOffVibration.value = pattern
			}
		}
		viewModelScope.launch {
			settingsRepository.getFlipSensitivity().collect { sensitivity ->
				_flipSensitivity.value = sensitivity
			}
		}
	}

	fun setScreenOffOnly(enabled: Boolean) {
		viewModelScope.launch {
			settingsRepository.setScreenOffOnlyEnabled(enabled)
		}
	}

	fun setPriorityDndEnabled(enabled: Boolean) {
		viewModelScope.launch {
			settingsRepository.setPriorityDndEnabled(enabled)
		}
	}

	fun setSoundEnabled(enabled: Boolean) {
		viewModelScope.launch {
			settingsRepository.setSoundEnabled(enabled)
		}
	}

	fun setVibrationEnabled(enabled: Boolean) {
		viewModelScope.launch {
			settingsRepository.setVibrationEnabled(enabled)
		}
	}

	fun setDndOnSound(sound: Sound) {
		viewModelScope.launch {
			settingsRepository.setDndOnSound(sound)
		}
	}

	fun setDndOffSound(sound: Sound) {
		viewModelScope.launch {
			settingsRepository.setDndOffSound(sound)
		}
	}

	fun setUseCustomVolume(enabled: Boolean) {
		viewModelScope.launch {
			settingsRepository.setUseCustomVolume(enabled)
		}
	}

	fun setCustomVolume(volume: Float) {
		viewModelScope.launch {
			settingsRepository.setCustomVolume(volume)
		}
	}

	fun setUseCustomVibration(enabled: Boolean) {
		viewModelScope.launch {
			settingsRepository.setUseCustomVibration(enabled)
		}
	}

	fun setCustomVibrationStrength(strength: Float) {
		viewModelScope.launch {
			settingsRepository.setCustomVibrationStrength(strength)
		}
	}

	fun setDndOnVibration(pattern: VibrationPattern) {
		viewModelScope.launch {
			settingsRepository.setDndOnVibration(pattern)
		}
	}

	fun setDndOffVibration(pattern: VibrationPattern) {
		viewModelScope.launch {
			settingsRepository.setDndOffVibration(pattern)
		}
	}

	fun setFlipSensitivity(sensitivity: Float) {
		viewModelScope.launch {
			settingsRepository.setFlipSensitivity(sensitivity)
		}
	}
	
	fun setNotificationsEnabled(enabled: Boolean) {
		viewModelScope.launch {
			settingsRepository.setNotificationsEnabled(enabled)
		}
	}

	fun setHighSensitivityModeEnabled(enabled: Boolean) {
		viewModelScope.launch {
			settingsRepository.setHighSensitivityModeEnabled(enabled)
		}
	}

	fun setBatterySaverOnFlipEnabled(enabled: Boolean) {
		viewModelScope.launch {
			settingsRepository.setBatterySaverOnFlipEnabled(enabled)
		}
	}

	fun setActivationDelay(seconds: Int) {
		viewModelScope.launch {
			settingsRepository.setActivationDelay(seconds)
		}
	}

	fun checkSecureSettingsPermission() {
		val permission = android.Manifest.permission.WRITE_SECURE_SETTINGS
		val isGranted = getApplication<Application>().checkSelfPermission(permission) == android.content.pm.PackageManager.PERMISSION_GRANTED
		_hasSecureSettingsPermission.value = isGranted
	}

	fun setDndOnCustomSoundUri(uri: String?) {
		viewModelScope.launch {
			settingsRepository.setDndOnCustomSoundUri(uri)
		}
	}

	fun setDndOffCustomSoundUri(uri: String?) {
		viewModelScope.launch {
			settingsRepository.setDndOffCustomSoundUri(uri)
		}
	}

	fun playSelectedSound(sound: Sound?) {
		if (sound == null) {
			android.util.Log.d("SettingsViewModel", "No sound selected")
			return
		}

		try {
			var mediaPlayer: android.media.MediaPlayer? = null
			
			if (sound == Sound.CUSTOM) {
				// For custom sounds, use the URI based on whether it's for DND on or off
				// Determine which custom sound URI to use based on the context
				val isForDndOn = sound == _dndOnSound.value
				val uri = if (isForDndOn) {
					_dndOnCustomSoundUri.value
				} else {
					_dndOffCustomSoundUri.value
				}

				android.util.Log.d("SettingsViewModel", "Using custom sound URI: $uri for ${if (isForDndOn) "DND ON" else "DND OFF"}")

				if (uri.isNullOrEmpty()) {
					android.util.Log.d("SettingsViewModel", "Custom sound selected but no URI available")
					return
				}

				try {
					val parsedUri = android.net.Uri.parse(uri)
					
					// Validate the URI is accessible
					try {
						getApplication<Application>().contentResolver.query(parsedUri, null, null, null, null)?.use { cursor ->
							if (!cursor.moveToFirst()) {
								android.util.Log.e("SettingsViewModel", "URI exists but content not accessible: $parsedUri")
							}
						} ?: run {
							android.util.Log.e("SettingsViewModel", "URI query returned null cursor: $parsedUri")
						}
					} catch (e: Exception) {
						android.util.Log.e("SettingsViewModel", "Error validating URI accessibility: ${e.message}", e)
						// Continue anyway - we'll try the direct approach
					}
					
					// Check if we have permission for this URI
					val persistedUriPermissions = getApplication<Application>().contentResolver.persistedUriPermissions
					val hasPermission = persistedUriPermissions.any { it.uri.toString() == parsedUri.toString() && it.isReadPermission }
					
					if (!hasPermission) {
						android.util.Log.e("SettingsViewModel", "No persisted permission for URI: $parsedUri")
						// Try to take permission again as a recovery mechanism
						try {
							getApplication<Application>().contentResolver.takePersistableUriPermission(
								parsedUri, 
								android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
							)
							android.util.Log.d("SettingsViewModel", "Successfully re-acquired permission for URI: $parsedUri")
						} catch (e: SecurityException) {
							android.util.Log.e("SettingsViewModel", "Failed to re-acquire permission: ${e.message}", e)
							// Continue anyway and try to play the sound
							android.util.Log.d("SettingsViewModel", "Attempting to play sound without explicit permission")
						}
					}
					
					// Try to create the MediaPlayer with the URI
					try {
						mediaPlayer = android.media.MediaPlayer().apply {
							setDataSource(getApplication(), parsedUri)
							prepare()
						}
					} catch (e: Exception) {
						android.util.Log.e("SettingsViewModel", "Error creating MediaPlayer: ${e.message}", e)
						// If this fails, try an alternative approach with content resolver
						try {
							val fileDescriptor = getApplication<Application>().contentResolver.openFileDescriptor(parsedUri, "r")?.fileDescriptor
							if (fileDescriptor != null) {
								android.util.Log.d("SettingsViewModel", "Using file descriptor approach for URI: $parsedUri")
								mediaPlayer = android.media.MediaPlayer().apply {
									setDataSource(fileDescriptor)
									prepare()
								}
							} else {
								android.util.Log.e("SettingsViewModel", "Failed to get file descriptor for URI: $parsedUri")
								return
							}
						} catch (e2: Exception) {
							android.util.Log.e("SettingsViewModel", "All approaches failed for URI: $parsedUri - ${e2.message}", e2)
							return
						}
					}
				} catch (e: SecurityException) {
					android.util.Log.e("SettingsViewModel", "Security exception accessing URI: ${e.message}", e)
					return
				} catch (e: IllegalStateException) {
					android.util.Log.e("SettingsViewModel", "IllegalStateException with MediaPlayer: ${e.message}", e)
					return
				} catch (e: IllegalArgumentException) {
					android.util.Log.e("SettingsViewModel", "IllegalArgumentException with URI: ${e.message}", e)
					return
				} catch (e: Exception) {
					android.util.Log.e("SettingsViewModel", "Error creating MediaPlayer for custom sound: ${e.message}", e)
					return
				}
			} else if (sound.soundResId == 0) {
				android.util.Log.d("SettingsViewModel", "Sound has no resource ID: ${sound.name}")
				return
			} else {
				mediaPlayer = android.media.MediaPlayer.create(getApplication(), sound.soundResId)
			}

			if (mediaPlayer == null) {
				android.util.Log.e(
					"SettingsViewModel",
					"Failed to create MediaPlayer for sound: ${sound.name}"
				)
				return
			}

			mediaPlayer.setVolume(
				if (useCustomVolume.value) customVolume.value else 1f,
				if (useCustomVolume.value) customVolume.value else 1f
			)
			mediaPlayer.setOnCompletionListener { mp -> mp.release() }
			mediaPlayer.start()
		} catch (e: Exception) {
			android.util.Log.e("SettingsViewModel", "Error playing sound: ${e.message}", e)
		}
	}

	fun playSelectedVibration(pattern: VibrationPattern) {
		val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
			val vibratorManager =
				getApplication<Application>().getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as android.os.VibratorManager
			vibratorManager.defaultVibrator
		} else {
			@Suppress("DEPRECATION")
			getApplication<Application>().getSystemService(android.content.Context.VIBRATOR_SERVICE) as android.os.Vibrator
		}

		try {
			val useCustomVibration = useCustomVibration.value
			val customStrength = if (useCustomVibration) {
				customVibrationStrength.value
			} else {
				1.0f
			}

			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
				val baseAmplitude = (255 * customStrength).toInt().coerceIn(1, 255)
				val amplitudes = IntArray(pattern.pattern.size) { index ->
					if (index % 2 == 0) 0 else baseAmplitude
				}

				val adjustedPattern = pattern.pattern.map { duration ->
					duration.coerceAtLeast(50L)
				}.toLongArray()

				vibrator.vibrate(android.os.VibrationEffect.createWaveform(adjustedPattern, amplitudes, -1))
			} else {
				@Suppress("DEPRECATION")
				vibrator.vibrate(pattern.pattern, -1)
			}
		} catch (e: Exception) {
			android.util.Log.e("SettingsViewModel", "Error during vibration: ${e.message}", e)
		}
	}
}
