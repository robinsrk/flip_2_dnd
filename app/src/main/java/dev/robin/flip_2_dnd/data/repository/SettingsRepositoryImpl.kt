package dev.robin.flip_2_dnd.data.repository

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.robin.flip_2_dnd.domain.repository.SettingsRepository
import dev.robin.flip_2_dnd.presentation.settings.FlashlightPattern
import dev.robin.flip_2_dnd.presentation.settings.Sound
import dev.robin.flip_2_dnd.presentation.settings.VibrationPattern
import dev.robin.flip_2_dnd.services.FlipDetectorService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "SettingsRepositoryImpl"
private const val PREFS_NAME = "flip_2_dnd_settings"
private const val KEY_SCREEN_OFF_ONLY = "screen_off_only"
private const val KEY_VIBRATION = "vibration"
private const val KEY_SOUND = "sound"
private const val KEY_PRIORITY_DND = "priority_dnd"
private const val KEY_DND_ON_SOUND = "dnd_on_sound"
private const val KEY_DND_OFF_SOUND = "dnd_off_sound"
private const val KEY_USE_CUSTOM_VOLUME = "use_custom_volume"
private const val KEY_CUSTOM_VOLUME = "custom_volume"
private const val KEY_USE_CUSTOM_VIBRATION = "use_custom_vibration"
private const val KEY_CUSTOM_VIBRATION_STRENGTH = "custom_vibration_strength"
private const val KEY_DND_ON_VIBRATION = "dnd_on_vibration"
private const val KEY_DND_OFF_VIBRATION = "dnd_off_vibration"
private const val KEY_FLIP_SENSITIVITY = "flip_sensitivity"
private const val KEY_DND_ON_CUSTOM_SOUND_URI = "dnd_on_custom_sound_uri"
private const val KEY_DND_OFF_CUSTOM_SOUND_URI = "dnd_off_custom_sound_uri"
private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
private const val KEY_HIGH_SENSITIVITY_MODE = "high_sensitivity_mode"
private const val KEY_BATTERY_SAVER_ON_FLIP = "battery_saver_on_flip"
private const val KEY_ACTIVATION_DELAY = "activation_delay"
private const val KEY_FLASHLIGHT_DETECTION = "flashlight_detection"
private const val KEY_MEDIA_PLAYBACK_DETECTION = "media_playback_detection"
private const val KEY_HEADPHONE_DETECTION = "headphone_detection"
private const val KEY_FLASHLIGHT_FEEDBACK_ENABLED = "flashlight_feedback_enabled"
private const val KEY_DND_ON_FLASHLIGHT_PATTERN = "dnd_on_flashlight_pattern"
private const val KEY_DND_OFF_FLASHLIGHT_PATTERN = "dnd_off_flashlight_pattern"
private const val KEY_DND_SCHEDULE_ENABLED = "schedule_enabled"
private const val KEY_DND_SCHEDULE_START_TIME = "schedule_start_time"
private const val KEY_DND_SCHEDULE_END_TIME = "schedule_end_time"
private const val KEY_DND_SCHEDULE_DAYS = "schedule_days"

private const val KEY_SOUND_SCHEDULE_ENABLED = "sound_schedule_enabled"
private const val KEY_SOUND_SCHEDULE_START_TIME = "sound_schedule_start_time"
private const val KEY_SOUND_SCHEDULE_END_TIME = "sound_schedule_end_time"
private const val KEY_SOUND_SCHEDULE_DAYS = "sound_schedule_days"

private const val KEY_VIBRATION_SCHEDULE_ENABLED = "vibration_schedule_enabled"
private const val KEY_VIBRATION_SCHEDULE_START_TIME = "vibration_schedule_start_time"
private const val KEY_VIBRATION_SCHEDULE_END_TIME = "vibration_schedule_end_time"
private const val KEY_VIBRATION_SCHEDULE_DAYS = "vibration_schedule_days"
private const val KEY_AUTO_START = "auto_start"

@Singleton
class SettingsRepositoryImpl @Inject constructor(
	@param:ApplicationContext private val appContext: Context
) : SettingsRepository {
	private val prefs: SharedPreferences =
		appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
	
	private val repositoryScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
	private var restartJob: Job? = null

	private val screenOffOnlyEnabled = MutableStateFlow(prefs.getBoolean(KEY_SCREEN_OFF_ONLY, true))
	private val vibrationEnabled = MutableStateFlow(prefs.getBoolean(KEY_VIBRATION, true))
	private val soundEnabled = MutableStateFlow(prefs.getBoolean(KEY_SOUND, true))
	private val priorityDndEnabled = MutableStateFlow(prefs.getBoolean(KEY_PRIORITY_DND, true))
	private val dndOnSound = MutableStateFlow(
		Sound.valueOf(
			prefs.getString(KEY_DND_ON_SOUND, Sound.SLUSH.name) ?: Sound.SLUSH.name
		)
	)
	private val dndOffSound = MutableStateFlow(
		Sound.valueOf(
			prefs.getString(KEY_DND_OFF_SOUND, Sound.WHISTLE.name) ?: Sound.WHISTLE.name
		)
	)
	private val useCustomVolume = MutableStateFlow(prefs.getBoolean(KEY_USE_CUSTOM_VOLUME, false))
	private val customVolume = MutableStateFlow(prefs.getFloat(KEY_CUSTOM_VOLUME, 1f))
	private val useCustomVibration =
		MutableStateFlow(prefs.getBoolean(KEY_USE_CUSTOM_VIBRATION, false))
	private val customVibrationStrength =
		MutableStateFlow(prefs.getFloat(KEY_CUSTOM_VIBRATION_STRENGTH, 1f))
	private val dndOnVibration = MutableStateFlow(
		VibrationPattern.valueOf(
			prefs.getString(
				KEY_DND_ON_VIBRATION,
				VibrationPattern.DOUBLE_PULSE.name
			) ?: VibrationPattern.DOUBLE_PULSE.name
		)
	)
	private val dndOffVibration = MutableStateFlow(
		VibrationPattern.valueOf(
			prefs.getString(
				KEY_DND_OFF_VIBRATION,
				VibrationPattern.SINGLE_PULSE.name
			) ?: VibrationPattern.SINGLE_PULSE.name
		)
	)
	private val flipSensitivity = MutableStateFlow(prefs.getFloat(KEY_FLIP_SENSITIVITY, 1f))
	private val dndOnCustomSoundUri = MutableStateFlow<String?>(prefs.getString(KEY_DND_ON_CUSTOM_SOUND_URI, null))
	private val dndOffCustomSoundUri = MutableStateFlow<String?>(prefs.getString(KEY_DND_OFF_CUSTOM_SOUND_URI, null))
	private val notificationsEnabled = MutableStateFlow(prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true))
	private val highSensitivityModeEnabled = MutableStateFlow(prefs.getBoolean(KEY_HIGH_SENSITIVITY_MODE, false))
	private val batterySaverOnFlipEnabled = MutableStateFlow(prefs.getBoolean(KEY_BATTERY_SAVER_ON_FLIP, false))
	private val activationDelay = MutableStateFlow(prefs.getInt(KEY_ACTIVATION_DELAY, 2))
	private val flashlightDetectionEnabled = MutableStateFlow(prefs.getBoolean(KEY_FLASHLIGHT_DETECTION, false))
	private val mediaPlaybackDetectionEnabled = MutableStateFlow(prefs.getBoolean(KEY_MEDIA_PLAYBACK_DETECTION, false))
	private val headphoneDetectionEnabled = MutableStateFlow(prefs.getBoolean(KEY_HEADPHONE_DETECTION, false))
	private val flashlightFeedbackEnabled = MutableStateFlow(prefs.getBoolean(KEY_FLASHLIGHT_FEEDBACK_ENABLED, false))
	private val dndOnFlashlightPattern = MutableStateFlow(
		FlashlightPattern.valueOf(
			prefs.getString(KEY_DND_ON_FLASHLIGHT_PATTERN, FlashlightPattern.DOUBLE_BLINK.name)
				?: FlashlightPattern.DOUBLE_BLINK.name
		)
	)
	private val dndOffFlashlightPattern = MutableStateFlow(
		FlashlightPattern.valueOf(
			prefs.getString(KEY_DND_OFF_FLASHLIGHT_PATTERN, FlashlightPattern.SINGLE_BLINK.name)
				?: FlashlightPattern.SINGLE_BLINK.name
		)
	)
	private val dndScheduleEnabled = MutableStateFlow(prefs.getBoolean(KEY_DND_SCHEDULE_ENABLED, false))
	private val dndScheduleStartTime = MutableStateFlow(prefs.getString(KEY_DND_SCHEDULE_START_TIME, "22:00") ?: "22:00")
	private val dndScheduleEndTime = MutableStateFlow(prefs.getString(KEY_DND_SCHEDULE_END_TIME, "07:00") ?: "07:00")
	private val dndScheduleDays = MutableStateFlow(
		prefs.getStringSet(KEY_DND_SCHEDULE_DAYS, setOf("1", "2", "3", "4", "5", "6", "7"))
			?.map { it.toInt() }?.toSet() ?: setOf(1, 2, 3, 4, 5, 6, 7)
	)

	private val soundScheduleEnabled = MutableStateFlow(prefs.getBoolean(KEY_SOUND_SCHEDULE_ENABLED, false))
	private val soundScheduleStartTime = MutableStateFlow(prefs.getString(KEY_SOUND_SCHEDULE_START_TIME, "22:00") ?: "22:00")
	private val soundScheduleEndTime = MutableStateFlow(prefs.getString(KEY_SOUND_SCHEDULE_END_TIME, "07:00") ?: "07:00")
	private val soundScheduleDays = MutableStateFlow(
		prefs.getStringSet(KEY_SOUND_SCHEDULE_DAYS, setOf("1", "2", "3", "4", "5", "6", "7"))
			?.map { it.toInt() }?.toSet() ?: setOf(1, 2, 3, 4, 5, 6, 7)
	)

	private val vibrationScheduleEnabled = MutableStateFlow(prefs.getBoolean(KEY_VIBRATION_SCHEDULE_ENABLED, false))
	private val vibrationScheduleStartTime = MutableStateFlow(prefs.getString(KEY_VIBRATION_SCHEDULE_START_TIME, "22:00") ?: "22:00")
	private val vibrationScheduleEndTime = MutableStateFlow(prefs.getString(KEY_VIBRATION_SCHEDULE_END_TIME, "07:00") ?: "07:00")
	private val vibrationScheduleDays = MutableStateFlow(
		prefs.getStringSet(KEY_VIBRATION_SCHEDULE_DAYS, setOf("1", "2", "3", "4", "5", "6", "7"))
			?.map { it.toInt() }?.toSet() ?: setOf(1, 2, 3, 4, 5, 6, 7)
	)

	private val autoStartEnabled = MutableStateFlow(prefs.getBoolean(KEY_AUTO_START, false))

	private fun restartFlipDetectorService() {
		restartJob?.cancel()
		restartJob = repositoryScope.launch {
			delay(500) // Debounce restarts
			try {
				val serviceIntent = Intent(appContext, FlipDetectorService::class.java)
				
				// Force a full restart by stopping first
				appContext.stopService(serviceIntent)
				delay(200) // Small delay to allow cleanup
				
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
					appContext.startForegroundService(serviceIntent)
				} else {
					appContext.startService(serviceIntent)
				}
				Log.d(TAG, "FlipDetectorService restarted successfully")
			} catch (e: Exception) {
				Log.e(TAG, "Error restarting FlipDetectorService: ${e.localizedMessage}", e)
			}
		}
	}

	override fun getScreenOffOnlyEnabled(): Flow<Boolean> = screenOffOnlyEnabled

	override suspend fun setScreenOffOnlyEnabled(enabled: Boolean) {
		prefs.edit().putBoolean(KEY_SCREEN_OFF_ONLY, enabled).apply()
		screenOffOnlyEnabled.value = enabled
		restartFlipDetectorService()
	}

	override fun getVibrationEnabled(): Flow<Boolean> = vibrationEnabled

	override suspend fun setVibrationEnabled(enabled: Boolean) {
		prefs.edit().putBoolean(KEY_VIBRATION, enabled).apply()
		vibrationEnabled.value = enabled
		restartFlipDetectorService()
	}

	override fun getSoundEnabled(): Flow<Boolean> = soundEnabled

	override suspend fun setSoundEnabled(enabled: Boolean) {
		prefs.edit().putBoolean(KEY_SOUND, enabled).apply()
		soundEnabled.value = enabled
		restartFlipDetectorService()
	}

	override fun getPriorityDndEnabled(): Flow<Boolean> = priorityDndEnabled

	override suspend fun setPriorityDndEnabled(enabled: Boolean) {
		prefs.edit().putBoolean(KEY_PRIORITY_DND, enabled).apply()
		priorityDndEnabled.value = enabled
		restartFlipDetectorService()
	}

	override fun getDndOnSound(): Flow<Sound> = dndOnSound

	override suspend fun setDndOnSound(sound: Sound) {
		prefs.edit().putString(KEY_DND_ON_SOUND, sound.name).apply()
		dndOnSound.value = sound
		restartFlipDetectorService()
	}

	override fun getDndOffSound(): Flow<Sound> = dndOffSound

	override suspend fun setDndOffSound(sound: Sound) {
		prefs.edit().putString(KEY_DND_OFF_SOUND, sound.name).apply()
		dndOffSound.value = sound
		restartFlipDetectorService()
	}

	override fun getUseCustomVolume(): Flow<Boolean> = useCustomVolume

	override suspend fun setUseCustomVolume(enabled: Boolean) {
		prefs.edit().putBoolean(KEY_USE_CUSTOM_VOLUME, enabled).apply()
		useCustomVolume.value = enabled
		restartFlipDetectorService()
	}

	override fun getCustomVolume(): Flow<Float> = customVolume

	override suspend fun setCustomVolume(volume: Float) {
		prefs.edit().putFloat(KEY_CUSTOM_VOLUME, volume).apply()
		customVolume.value = volume
		restartFlipDetectorService()
	}

	override fun getUseCustomVibration(): Flow<Boolean> = useCustomVibration

	override suspend fun setUseCustomVibration(enabled: Boolean) {
		prefs.edit().putBoolean(KEY_USE_CUSTOM_VIBRATION, enabled).apply()
		useCustomVibration.value = enabled
		restartFlipDetectorService()
	}

	override fun getCustomVibrationStrength(): Flow<Float> = customVibrationStrength

	override suspend fun setCustomVibrationStrength(strength: Float) {
		prefs.edit().putFloat(KEY_CUSTOM_VIBRATION_STRENGTH, strength).apply()
		customVibrationStrength.value = strength
		restartFlipDetectorService()
	}

	override fun getDndOnVibration(): Flow<VibrationPattern> = dndOnVibration

	override suspend fun setDndOnVibration(pattern: VibrationPattern) {
		prefs.edit().putString(KEY_DND_ON_VIBRATION, pattern.name).apply()
		dndOnVibration.value = pattern
		restartFlipDetectorService()
	}

	override fun getDndOffVibration(): Flow<VibrationPattern> = dndOffVibration

	override suspend fun setDndOffVibration(pattern: VibrationPattern) {
		prefs.edit().putString(KEY_DND_OFF_VIBRATION, pattern.name).apply()
		dndOffVibration.value = pattern
		restartFlipDetectorService()
	}

	override fun getFlipSensitivity(): Flow<Float> = flipSensitivity

	override suspend fun setFlipSensitivity(sensitivity: Float) {
		prefs.edit().putFloat(KEY_FLIP_SENSITIVITY, sensitivity).apply()
		flipSensitivity.value = sensitivity
		restartFlipDetectorService()
	}

	override fun getDndOnCustomSoundUri(): Flow<String?> = dndOnCustomSoundUri

	override suspend fun setDndOnCustomSoundUri(uri: String?) {
		prefs.edit().putString(KEY_DND_ON_CUSTOM_SOUND_URI, uri).apply()
		dndOnCustomSoundUri.value = uri
		restartFlipDetectorService()
	}

	override fun getDndOffCustomSoundUri(): Flow<String?> = dndOffCustomSoundUri

	override suspend fun setDndOffCustomSoundUri(uri: String?) {
		prefs.edit().putString(KEY_DND_OFF_CUSTOM_SOUND_URI, uri).apply()
		dndOffCustomSoundUri.value = uri
		restartFlipDetectorService()
	}

	override fun getNotificationsEnabled(): Flow<Boolean> = notificationsEnabled

	override suspend fun setNotificationsEnabled(enabled: Boolean) {
		prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
		notificationsEnabled.value = enabled
		restartFlipDetectorService()
	}

	override fun getHighSensitivityModeEnabled(): Flow<Boolean> = highSensitivityModeEnabled

	override suspend fun setHighSensitivityModeEnabled(enabled: Boolean) {
		prefs.edit().putBoolean(KEY_HIGH_SENSITIVITY_MODE, enabled).apply()
		highSensitivityModeEnabled.value = enabled
		restartFlipDetectorService()
	}

	override fun getBatterySaverOnFlipEnabled(): Flow<Boolean> = batterySaverOnFlipEnabled

	override suspend fun setBatterySaverOnFlipEnabled(enabled: Boolean) {
		prefs.edit().putBoolean(KEY_BATTERY_SAVER_ON_FLIP, enabled).apply()
		batterySaverOnFlipEnabled.value = enabled
		restartFlipDetectorService()
	}

	override fun getActivationDelay(): Flow<Int> = activationDelay

	override suspend fun setActivationDelay(seconds: Int) {
		prefs.edit().putInt(KEY_ACTIVATION_DELAY, seconds).apply()
		activationDelay.value = seconds
		restartFlipDetectorService()
	}

	override fun getFlashlightDetectionEnabled(): Flow<Boolean> = flashlightDetectionEnabled

	override suspend fun setFlashlightDetectionEnabled(enabled: Boolean) {
		prefs.edit().putBoolean(KEY_FLASHLIGHT_DETECTION, enabled).apply()
		flashlightDetectionEnabled.value = enabled
		restartFlipDetectorService()
	}

	override fun getMediaPlaybackDetectionEnabled(): Flow<Boolean> = mediaPlaybackDetectionEnabled

	override suspend fun setMediaPlaybackDetectionEnabled(enabled: Boolean) {
		prefs.edit().putBoolean(KEY_MEDIA_PLAYBACK_DETECTION, enabled).apply()
		mediaPlaybackDetectionEnabled.value = enabled
		restartFlipDetectorService()
	}

	override fun getHeadphoneDetectionEnabled(): Flow<Boolean> = headphoneDetectionEnabled

	override suspend fun setHeadphoneDetectionEnabled(enabled: Boolean) {
		prefs.edit().putBoolean(KEY_HEADPHONE_DETECTION, enabled).apply()
		headphoneDetectionEnabled.value = enabled
		restartFlipDetectorService()
	}

	override fun getFlashlightFeedbackEnabled(): Flow<Boolean> = flashlightFeedbackEnabled

	override suspend fun setFlashlightFeedbackEnabled(enabled: Boolean) {
		prefs.edit().putBoolean(KEY_FLASHLIGHT_FEEDBACK_ENABLED, enabled).apply()
		flashlightFeedbackEnabled.value = enabled
		restartFlipDetectorService()
	}

	override fun getDndOnFlashlightPattern(): Flow<FlashlightPattern> = dndOnFlashlightPattern

	override suspend fun setDndOnFlashlightPattern(pattern: FlashlightPattern) {
		prefs.edit().putString(KEY_DND_ON_FLASHLIGHT_PATTERN, pattern.name).apply()
		dndOnFlashlightPattern.value = pattern
		restartFlipDetectorService()
	}

	override fun getDndOffFlashlightPattern(): Flow<FlashlightPattern> = dndOffFlashlightPattern

	override suspend fun setDndOffFlashlightPattern(pattern: FlashlightPattern) {
		prefs.edit().putString(KEY_DND_OFF_FLASHLIGHT_PATTERN, pattern.name).apply()
		dndOffFlashlightPattern.value = pattern
		restartFlipDetectorService()
	}

	override fun getDndScheduleEnabled(): Flow<Boolean> = dndScheduleEnabled

	override suspend fun setDndScheduleEnabled(enabled: Boolean) {
		prefs.edit().putBoolean(KEY_DND_SCHEDULE_ENABLED, enabled).apply()
		dndScheduleEnabled.value = enabled
		restartFlipDetectorService()
	}

	override fun getDndScheduleStartTime(): Flow<String> = dndScheduleStartTime

	override suspend fun setDndScheduleStartTime(startTime: String) {
		prefs.edit().putString(KEY_DND_SCHEDULE_START_TIME, startTime).apply()
		dndScheduleStartTime.value = startTime
		restartFlipDetectorService()
	}

	override fun getDndScheduleEndTime(): Flow<String> = dndScheduleEndTime

	override suspend fun setDndScheduleEndTime(endTime: String) {
		prefs.edit().putString(KEY_DND_SCHEDULE_END_TIME, endTime).apply()
		dndScheduleEndTime.value = endTime
		restartFlipDetectorService()
	}

	override fun getDndScheduleDays(): Flow<Set<Int>> = dndScheduleDays

	override suspend fun setDndScheduleDays(days: Set<Int>) {
		prefs.edit().putStringSet(KEY_DND_SCHEDULE_DAYS, days.map { it.toString() }.toSet()).apply()
		dndScheduleDays.value = days
		restartFlipDetectorService()
	}

	override fun getSoundScheduleEnabled(): Flow<Boolean> = soundScheduleEnabled

	override suspend fun setSoundScheduleEnabled(enabled: Boolean) {
		prefs.edit().putBoolean(KEY_SOUND_SCHEDULE_ENABLED, enabled).apply()
		soundScheduleEnabled.value = enabled
		restartFlipDetectorService()
	}

	override fun getSoundScheduleStartTime(): Flow<String> = soundScheduleStartTime

	override suspend fun setSoundScheduleStartTime(startTime: String) {
		prefs.edit().putString(KEY_SOUND_SCHEDULE_START_TIME, startTime).apply()
		soundScheduleStartTime.value = startTime
		restartFlipDetectorService()
	}

	override fun getSoundScheduleEndTime(): Flow<String> = soundScheduleEndTime

	override suspend fun setSoundScheduleEndTime(endTime: String) {
		prefs.edit().putString(KEY_SOUND_SCHEDULE_END_TIME, endTime).apply()
		soundScheduleEndTime.value = endTime
		restartFlipDetectorService()
	}

	override fun getSoundScheduleDays(): Flow<Set<Int>> = soundScheduleDays

	override suspend fun setSoundScheduleDays(days: Set<Int>) {
		prefs.edit().putStringSet(KEY_SOUND_SCHEDULE_DAYS, days.map { it.toString() }.toSet()).apply()
		soundScheduleDays.value = days
		restartFlipDetectorService()
	}

	override fun getVibrationScheduleEnabled(): Flow<Boolean> = vibrationScheduleEnabled

	override suspend fun setVibrationScheduleEnabled(enabled: Boolean) {
		prefs.edit().putBoolean(KEY_VIBRATION_SCHEDULE_ENABLED, enabled).apply()
		vibrationScheduleEnabled.value = enabled
		restartFlipDetectorService()
	}

	override fun getVibrationScheduleStartTime(): Flow<String> = vibrationScheduleStartTime

	override suspend fun setVibrationScheduleStartTime(startTime: String) {
		prefs.edit().putString(KEY_VIBRATION_SCHEDULE_START_TIME, startTime).apply()
		vibrationScheduleStartTime.value = startTime
		restartFlipDetectorService()
	}

	override fun getVibrationScheduleEndTime(): Flow<String> = vibrationScheduleEndTime

	override suspend fun setVibrationScheduleEndTime(endTime: String) {
		prefs.edit().putString(KEY_VIBRATION_SCHEDULE_END_TIME, endTime).apply()
		vibrationScheduleEndTime.value = endTime
		restartFlipDetectorService()
	}

	override suspend fun setVibrationScheduleDays(days: Set<Int>) {
		prefs.edit().putStringSet(KEY_VIBRATION_SCHEDULE_DAYS, days.map { it.toString() }.toSet()).apply()
		vibrationScheduleDays.value = days
		restartFlipDetectorService()
	}

	override fun getAutoStartEnabled(): Flow<Boolean> = autoStartEnabled

	override suspend fun setAutoStartEnabled(enabled: Boolean) {
		prefs.edit().putBoolean(KEY_AUTO_START, enabled).apply()
		autoStartEnabled.value = enabled
		// No need to restart service for this, as it's only checked on boot
	}

	override fun getVibrationScheduleDays(): Flow<Set<Int>> = vibrationScheduleDays
}
