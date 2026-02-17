package dev.robin.flip_2_dnd.presentation.settings

import android.app.Application
import android.content.Context
import android.hardware.camera2.CameraManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.robin.flip_2_dnd.domain.repository.SettingsRepository
import dev.robin.flip_2_dnd.domain.repository.ActivationMode
import dev.robin.flip_2_dnd.domain.repository.DndMode
import dev.robin.flip_2_dnd.domain.repository.RingerMode
import kotlinx.coroutines.delay
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
	val availableFlashlightPatterns = FlashlightPattern.entries
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

	private val _flashlightDetectionEnabled = MutableStateFlow(false)
	val flashlightDetectionEnabled = _flashlightDetectionEnabled.asStateFlow()

	private val _mediaPlaybackDetectionEnabled = MutableStateFlow(false)
	val mediaPlaybackDetectionEnabled = _mediaPlaybackDetectionEnabled.asStateFlow()

	private val _headphoneDetectionEnabled = MutableStateFlow(false)
	val headphoneDetectionEnabled = _headphoneDetectionEnabled.asStateFlow()

	private val _proximityDetectionEnabled = MutableStateFlow(false)
	val proximityDetectionEnabled = _proximityDetectionEnabled.asStateFlow()

	private val _flashlightFeedbackEnabled = MutableStateFlow(false)
	val flashlightFeedbackEnabled = _flashlightFeedbackEnabled.asStateFlow()

	private val _feedbackWithFlashlightOn = MutableStateFlow(true)
	val feedbackWithFlashlightOn = _feedbackWithFlashlightOn.asStateFlow()

	private val _dndOnFlashlightPattern = MutableStateFlow(FlashlightPattern.DOUBLE_BLINK)
	val dndOnFlashlightPattern = _dndOnFlashlightPattern.asStateFlow()

	private val _dndOffFlashlightPattern = MutableStateFlow(FlashlightPattern.SINGLE_BLINK)
	val dndOffFlashlightPattern = _dndOffFlashlightPattern.asStateFlow()

	private val _dndScheduleEnabled = MutableStateFlow(false)
    val dndScheduleEnabled = _dndScheduleEnabled.asStateFlow()

    private val _dndScheduleStartTime = MutableStateFlow("22:00")
    val dndScheduleStartTime = _dndScheduleStartTime.asStateFlow()

    private val _dndScheduleEndTime = MutableStateFlow("07:00")
    val dndScheduleEndTime = _dndScheduleEndTime.asStateFlow()

    private val _dndScheduleDays = MutableStateFlow(setOf(1, 2, 3, 4, 5, 6, 7))
    val dndScheduleDays = _dndScheduleDays.asStateFlow()

	private val _autoStartEnabled = MutableStateFlow(false)
	val autoStartEnabled = _autoStartEnabled.asStateFlow()

    private val _soundScheduleEnabled = MutableStateFlow(false)
    val soundScheduleEnabled = _soundScheduleEnabled.asStateFlow()

    private val _soundScheduleStartTime = MutableStateFlow("22:00")
    val soundScheduleStartTime = _soundScheduleStartTime.asStateFlow()

    private val _soundScheduleEndTime = MutableStateFlow("07:00")
    val soundScheduleEndTime = _soundScheduleEndTime.asStateFlow()

    private val _soundScheduleDays = MutableStateFlow(setOf(1, 2, 3, 4, 5, 6, 7))
    val soundScheduleDays = _soundScheduleDays.asStateFlow()

    private val _vibrationScheduleEnabled = MutableStateFlow(false)
    val vibrationScheduleEnabled = _vibrationScheduleEnabled.asStateFlow()

    private val _vibrationScheduleStartTime = MutableStateFlow("22:00")
    val vibrationScheduleStartTime = _vibrationScheduleStartTime.asStateFlow()

    private val _vibrationScheduleEndTime = MutableStateFlow("07:00")
    val vibrationScheduleEndTime = _vibrationScheduleEndTime.asStateFlow()

    private val _vibrationScheduleDays = MutableStateFlow(setOf(1, 2, 3, 4, 5, 6, 7))
    val vibrationScheduleDays = _vibrationScheduleDays.asStateFlow()

    private val _flashlightScheduleEnabled = MutableStateFlow(false)
    val flashlightScheduleEnabled = _flashlightScheduleEnabled.asStateFlow()

    private val _flashlightScheduleStartTime = MutableStateFlow("22:00")
    val flashlightScheduleStartTime = _flashlightScheduleStartTime.asStateFlow()

    private val _flashlightScheduleEndTime = MutableStateFlow("07:00")
    val flashlightScheduleEndTime = _flashlightScheduleEndTime.asStateFlow()

    private val _flashlightScheduleDays = MutableStateFlow(setOf(1, 2, 3, 4, 5, 6, 7))
    val flashlightScheduleDays = _flashlightScheduleDays.asStateFlow()

    private val _highSensitivityScheduleEnabled = MutableStateFlow(false)
    val highSensitivityScheduleEnabled = _highSensitivityScheduleEnabled.asStateFlow()

    private val _highSensitivityScheduleStartTime = MutableStateFlow("22:00")
    val highSensitivityScheduleStartTime = _highSensitivityScheduleStartTime.asStateFlow()

    private val _highSensitivityScheduleEndTime = MutableStateFlow("07:00")
    val highSensitivityScheduleEndTime = _highSensitivityScheduleEndTime.asStateFlow()

    private val _highSensitivityScheduleDays = MutableStateFlow(setOf(1, 2, 3, 4, 5, 6, 7))
    val highSensitivityScheduleDays = _highSensitivityScheduleDays.asStateFlow()

	private val _activationMode = MutableStateFlow(ActivationMode.DND)
	val activationMode = _activationMode.asStateFlow()

	private val _dndMode = MutableStateFlow(DndMode.TOTAL_SILENCE)
	val dndMode = _dndMode.asStateFlow()

	private val _ringerMode = MutableStateFlow(RingerMode.SILENT)
	val ringerMode = _ringerMode.asStateFlow()

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
		viewModelScope.launch {
			settingsRepository.getFlashlightDetectionEnabled().collect { enabled ->
				_flashlightDetectionEnabled.value = enabled
			}
		}
		viewModelScope.launch {
			settingsRepository.getMediaPlaybackDetectionEnabled().collect { enabled ->
				_mediaPlaybackDetectionEnabled.value = enabled
			}
		}
		viewModelScope.launch {
			settingsRepository.getHeadphoneDetectionEnabled().collect { enabled ->
				_headphoneDetectionEnabled.value = enabled
			}
		}
		viewModelScope.launch {
			settingsRepository.getProximityDetectionEnabled().collect { enabled ->
				_proximityDetectionEnabled.value = enabled
			}
		}
		viewModelScope.launch {
			settingsRepository.getFlashlightFeedbackEnabled().collect { enabled ->
				_flashlightFeedbackEnabled.value = enabled
			}
		}
		viewModelScope.launch {
			settingsRepository.getFeedbackWithFlashlightOn().collect { enabled ->
				_feedbackWithFlashlightOn.value = enabled
			}
		}
		viewModelScope.launch {
			settingsRepository.getDndOnFlashlightPattern().collect { pattern ->
				_dndOnFlashlightPattern.value = pattern
			}
		}
		viewModelScope.launch {
			settingsRepository.getDndOffFlashlightPattern().collect { pattern ->
				_dndOffFlashlightPattern.value = pattern
			}
		}
		viewModelScope.launch {
			settingsRepository.getDndScheduleEnabled().collect { enabled ->
				_dndScheduleEnabled.value = enabled
			}
		}
		viewModelScope.launch {
			settingsRepository.getDndScheduleStartTime().collect { time ->
				_dndScheduleStartTime.value = time
			}
		}
		viewModelScope.launch {
			settingsRepository.getDndScheduleEndTime().collect { time ->
				_dndScheduleEndTime.value = time
			}
		}
		viewModelScope.launch {
			settingsRepository.getDndScheduleDays().collect { days ->
				_dndScheduleDays.value = days
			}
		}
		viewModelScope.launch {
			settingsRepository.getSoundScheduleEnabled().collect { enabled ->
				_soundScheduleEnabled.value = enabled
			}
		}
		viewModelScope.launch {
			settingsRepository.getSoundScheduleStartTime().collect { time ->
				_soundScheduleStartTime.value = time
			}
		}
		viewModelScope.launch {
			settingsRepository.getSoundScheduleEndTime().collect { time ->
				_soundScheduleEndTime.value = time
			}
		}
		viewModelScope.launch {
			settingsRepository.getSoundScheduleDays().collect { days ->
				_soundScheduleDays.value = days
			}
		}
		viewModelScope.launch {
			settingsRepository.getVibrationScheduleEnabled().collect { enabled ->
				_vibrationScheduleEnabled.value = enabled
			}
		}
		viewModelScope.launch {
			settingsRepository.getVibrationScheduleStartTime().collect { time ->
				_vibrationScheduleStartTime.value = time
			}
		}
		viewModelScope.launch {
			settingsRepository.getVibrationScheduleEndTime().collect { time ->
				_vibrationScheduleEndTime.value = time
			}
		}
		viewModelScope.launch {
			settingsRepository.getVibrationScheduleDays().collect { days ->
				_vibrationScheduleDays.value = days
			}
		}
		viewModelScope.launch {
			settingsRepository.getFlashlightScheduleEnabled().collect { enabled ->
				_flashlightScheduleEnabled.value = enabled
			}
		}
		viewModelScope.launch {
			settingsRepository.getFlashlightScheduleStartTime().collect { time ->
				_flashlightScheduleStartTime.value = time
			}
		}
		viewModelScope.launch {
			settingsRepository.getFlashlightScheduleEndTime().collect { time ->
				_flashlightScheduleEndTime.value = time
			}
		}
		viewModelScope.launch {
			settingsRepository.getFlashlightScheduleDays().collect { days ->
				_flashlightScheduleDays.value = days
			}
		}
		viewModelScope.launch {
			settingsRepository.getHighSensitivityScheduleEnabled().collect { enabled ->
				_highSensitivityScheduleEnabled.value = enabled
			}
		}
		viewModelScope.launch {
			settingsRepository.getHighSensitivityScheduleStartTime().collect { time ->
				_highSensitivityScheduleStartTime.value = time
			}
		}
		viewModelScope.launch {
			settingsRepository.getHighSensitivityScheduleEndTime().collect { time ->
				_highSensitivityScheduleEndTime.value = time
			}
		}
		viewModelScope.launch {
			settingsRepository.getHighSensitivityScheduleDays().collect { days ->
				_highSensitivityScheduleDays.value = days
			}
		}
		viewModelScope.launch {
			settingsRepository.getAutoStartEnabled().collect { enabled ->
				_autoStartEnabled.value = enabled
			}
		}
		viewModelScope.launch {
			settingsRepository.getActivationMode().collect { mode ->
				_activationMode.value = mode
			}
		}
		viewModelScope.launch {
			settingsRepository.getDndMode().collect { mode ->
				_dndMode.value = mode
			}
		}
		viewModelScope.launch {
			settingsRepository.getRingerMode().collect { mode ->
				_ringerMode.value = mode
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

	fun setFlashlightDetectionEnabled(enabled: Boolean) {
		viewModelScope.launch {
			settingsRepository.setFlashlightDetectionEnabled(enabled)
		}
	}

	fun setMediaPlaybackDetectionEnabled(enabled: Boolean) {
		viewModelScope.launch {
			settingsRepository.setMediaPlaybackDetectionEnabled(enabled)
		}
	}

	fun setHeadphoneDetectionEnabled(enabled: Boolean) {
		viewModelScope.launch {
			settingsRepository.setHeadphoneDetectionEnabled(enabled)
		}
	}

	fun setProximityDetectionEnabled(enabled: Boolean) {
		viewModelScope.launch {
			settingsRepository.setProximityDetectionEnabled(enabled)
		}
	}

	fun setFlashlightFeedbackEnabled(enabled: Boolean) {
		viewModelScope.launch {
			settingsRepository.setFlashlightFeedbackEnabled(enabled)
		}
	}

	fun setFeedbackWithFlashlightOn(enabled: Boolean) {
		viewModelScope.launch {
			settingsRepository.setFeedbackWithFlashlightOn(enabled)
		}
	}

	fun setDndOnFlashlightPattern(pattern: FlashlightPattern) {
		viewModelScope.launch {
			settingsRepository.setDndOnFlashlightPattern(pattern)
		}
	}

	fun setDndOffFlashlightPattern(pattern: FlashlightPattern) {
		viewModelScope.launch {
			settingsRepository.setDndOffFlashlightPattern(pattern)
		}
	}

	fun setDndScheduleEnabled(enabled: Boolean) {
		viewModelScope.launch {
			settingsRepository.setDndScheduleEnabled(enabled)
		}
	}

	fun setDndScheduleStartTime(startTime: String) {
		viewModelScope.launch {
			settingsRepository.setDndScheduleStartTime(startTime)
		}
	}

	fun setDndScheduleEndTime(endTime: String) {
		viewModelScope.launch {
			settingsRepository.setDndScheduleEndTime(endTime)
		}
	}

	fun setDndScheduleDays(days: Set<Int>) {
		viewModelScope.launch {
			settingsRepository.setDndScheduleDays(days)
		}
	}

	fun setSoundScheduleEnabled(enabled: Boolean) {
		viewModelScope.launch {
			settingsRepository.setSoundScheduleEnabled(enabled)
		}
	}

	fun setSoundScheduleStartTime(startTime: String) {
		viewModelScope.launch {
			settingsRepository.setSoundScheduleStartTime(startTime)
		}
	}

	fun setSoundScheduleEndTime(endTime: String) {
		viewModelScope.launch {
			settingsRepository.setSoundScheduleEndTime(endTime)
		}
	}

	fun setSoundScheduleDays(days: Set<Int>) {
		viewModelScope.launch {
			settingsRepository.setSoundScheduleDays(days)
		}
	}

	fun setVibrationScheduleEnabled(enabled: Boolean) {
		viewModelScope.launch {
			settingsRepository.setVibrationScheduleEnabled(enabled)
		}
	}

	fun setVibrationScheduleStartTime(startTime: String) {
		viewModelScope.launch {
			settingsRepository.setVibrationScheduleStartTime(startTime)
		}
	}

	fun setVibrationScheduleEndTime(endTime: String) {
		viewModelScope.launch {
			settingsRepository.setVibrationScheduleEndTime(endTime)
		}
	}

	fun setVibrationScheduleDays(days: Set<Int>) {
		viewModelScope.launch {
			settingsRepository.setVibrationScheduleDays(days)
		}
	}

	fun setFlashlightScheduleEnabled(enabled: Boolean) {
		viewModelScope.launch {
			settingsRepository.setFlashlightScheduleEnabled(enabled)
		}
	}

	fun setFlashlightScheduleStartTime(startTime: String) {
		viewModelScope.launch {
			settingsRepository.setFlashlightScheduleStartTime(startTime)
		}
	}

	fun setFlashlightScheduleEndTime(endTime: String) {
		viewModelScope.launch {
			settingsRepository.setFlashlightScheduleEndTime(endTime)
		}
	}

	fun setFlashlightScheduleDays(days: Set<Int>) {
		viewModelScope.launch {
			settingsRepository.setFlashlightScheduleDays(days)
		}
	}

	fun setActivationMode(mode: ActivationMode) {
		viewModelScope.launch {
			settingsRepository.setActivationMode(mode)
		}
	}

	fun setDndMode(mode: DndMode) {
		viewModelScope.launch {
			settingsRepository.setDndMode(mode)
		}
	}

	fun setRingerMode(mode: RingerMode) {
		viewModelScope.launch {
			settingsRepository.setRingerMode(mode)
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

	fun playSelectedSound(sound: Sound?, isForDndOn: Boolean = true) {
		if (sound == null) {
			android.util.Log.d("SettingsViewModel", "No sound selected")
			return
		}

		try {
			var mediaPlayer: android.media.MediaPlayer? = null
			
			if (sound == Sound.CUSTOM) {
				// For custom sounds, use the URI based on whether it's for DND on or off
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

	fun playSelectedFlashlightPattern(pattern: FlashlightPattern) {
		val cameraManager = getApplication<Application>().getSystemService(Context.CAMERA_SERVICE) as CameraManager
		val cameraId = try {
			cameraManager.cameraIdList.firstOrNull { id ->
				val characteristics = cameraManager.getCameraCharacteristics(id)
				characteristics.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
			}
		} catch (e: Exception) {
			null
		}

		if (cameraId != null) {
			viewModelScope.launch {
				try {
					pattern.pattern.forEachIndexed { index, duration ->
						val state = index % 2 != 0 // Odd indices are ON (if we follow 0, on, off, on...)
						// Wait, FlashlightPattern uses (delay, on, off, on...)
						// Pattern: 0, 200, 200 means 0 delay, 200ms ON, 200ms OFF
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
					android.util.Log.e("SettingsViewModel", "Error playing flashlight pattern: ${e.message}")
				}
			}
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
	fun setHighSensitivityScheduleEnabled(enabled: Boolean) {
		viewModelScope.launch {
			settingsRepository.setHighSensitivityScheduleEnabled(enabled)
		}
	}

	fun setHighSensitivityScheduleStartTime(startTime: String) {
		viewModelScope.launch {
			settingsRepository.setHighSensitivityScheduleStartTime(startTime)
		}
	}

	fun setHighSensitivityScheduleEndTime(endTime: String) {
		viewModelScope.launch {
			settingsRepository.setHighSensitivityScheduleEndTime(endTime)
		}
	}

	fun setHighSensitivityScheduleDays(days: Set<Int>) {
		viewModelScope.launch {
			settingsRepository.setHighSensitivityScheduleDays(days)
		}
	}

	fun setAutoStartEnabled(enabled: Boolean) {
		viewModelScope.launch {
			settingsRepository.setAutoStartEnabled(enabled)
		}
	}
}
