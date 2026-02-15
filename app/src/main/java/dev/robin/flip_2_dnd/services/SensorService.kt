package dev.robin.flip_2_dnd.services

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import dev.robin.flip_2_dnd.domain.model.PhoneOrientation
import dev.robin.flip_2_dnd.data.repository.SettingsRepositoryImpl
import dev.robin.flip_2_dnd.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

private const val TAG = "SensorService"

class SensorService(
	context: Context,
	private val settingsRepository: SettingsRepository
) {
	private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
	private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
	private val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
	private val proximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

	private val _orientation = MutableStateFlow(PhoneOrientation.FACE_UP)
	val orientation: StateFlow<PhoneOrientation> = _orientation

	private val _isProximityCovered = MutableStateFlow(false)
	val isProximityCovered: StateFlow<Boolean> = _isProximityCovered

	private val _accelerometerData = MutableStateFlow(FloatArray(3) { 0f })
	val accelerometerData: StateFlow<FloatArray> = _accelerometerData

	private val _gyroscopeData = MutableStateFlow(FloatArray(3) { 0f })
	val gyroscopeData: StateFlow<FloatArray> = _gyroscopeData

	private var lastAccelReading = FloatArray(3)
	private var lastGyroReading = FloatArray(3)
	private var filteredAccel = FloatArray(3)
	private val alpha = 0.15f // Smoothing factor for low-pass filter
	private var isProcessing = false
	private var isRegistered = false
	private var sensitivity = 0.5f
	private var highSensitivityMode = false
	private var highSensitivityScheduleEnabled = false
	private var highSensitivityScheduleStartTime = "22:00"
	private var highSensitivityScheduleEndTime = "07:00"
	private var highSensitivityScheduleDays = setOf(1, 2, 3, 4, 5, 6, 7)

	init {
		if (accelerometer == null) {
			Log.e(TAG, "No accelerometer sensor found!")
		}
		if (gyroscope == null) {
			Log.e(TAG, "No gyroscope sensor found!")
		}

		// Observe sensitivity changes
		CoroutineScope(Dispatchers.Main).launch {
			settingsRepository.getFlipSensitivity().collect { newSensitivity ->
				sensitivity = newSensitivity
				Log.d(TAG, "Sensitivity updated to: $sensitivity")
			}
		}

		// Observe high sensitivity mode changes
		CoroutineScope(Dispatchers.Main).launch {
			settingsRepository.getHighSensitivityModeEnabled().collect { enabled ->
				highSensitivityMode = enabled
				Log.d(TAG, "High sensitivity mode updated to: $highSensitivityMode")
			}
		}

		CoroutineScope(Dispatchers.Main).launch {
			settingsRepository.getHighSensitivityScheduleEnabled().collect { enabled ->
				highSensitivityScheduleEnabled = enabled
			}
		}

		CoroutineScope(Dispatchers.Main).launch {
			settingsRepository.getHighSensitivityScheduleStartTime().collect { time ->
				highSensitivityScheduleStartTime = time
			}
		}

		CoroutineScope(Dispatchers.Main).launch {
			settingsRepository.getHighSensitivityScheduleEndTime().collect { time ->
				highSensitivityScheduleEndTime = time
			}
		}

		CoroutineScope(Dispatchers.Main).launch {
			settingsRepository.getHighSensitivityScheduleDays().collect { days ->
				highSensitivityScheduleDays = days
			}
		}
	}

	private val sensorListener = object : SensorEventListener {
		override fun onSensorChanged(event: SensorEvent) {
			when (event.sensor.type) {
				Sensor.TYPE_ACCELEROMETER -> {
					val currentAccel = event.values
					for (i in 0..2) {
						filteredAccel[i] = alpha * currentAccel[i] + (1 - alpha) * filteredAccel[i]
					}
					lastAccelReading = filteredAccel.clone()
					_accelerometerData.value = filteredAccel.clone()
					processOrientation()
				}

				Sensor.TYPE_GYROSCOPE -> {
					lastGyroReading = event.values.clone()
					_gyroscopeData.value = event.values.clone()
//					Log.d(TAG, "Gyroscope data: ${lastGyroReading.contentToString()}")
				}

				Sensor.TYPE_PROXIMITY -> {
					val distance = event.values[0]
					_isProximityCovered.value = distance < (proximity?.maximumRange ?: 0f)
					Log.d(TAG, "Proximity distance: $distance, covered: ${_isProximityCovered.value}")
				}
			}
		}

		override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
			Log.d(TAG, "Sensor accuracy changed: ${sensor?.name}, accuracy: $accuracy")
		}
	}

	fun startMonitoring() {
		if (isRegistered) {
			Log.d(TAG, "Sensors already registered")
			return
		}

		if (accelerometer == null || gyroscope == null) {
			Log.e(
				TAG,
				"Required sensors not available - Accelerometer: ${accelerometer != null}, Gyroscope: ${gyroscope != null}"
			)
			return
		}

		var success = true

		success = success && sensorManager.registerListener(
			sensorListener,
			accelerometer,
			SensorManager.SENSOR_DELAY_GAME
		)
		if (!success) {
			Log.e(TAG, "Failed to register accelerometer")
			return
		}

		success = success && sensorManager.registerListener(
			sensorListener,
			gyroscope,
			SensorManager.SENSOR_DELAY_GAME
		)
		if (!success) {
			Log.e(TAG, "Failed to register gyroscope")
			sensorManager.unregisterListener(sensorListener)
			return
		}

		if (proximity != null) {
			success = success && sensorManager.registerListener(
				sensorListener,
				proximity,
				SensorManager.SENSOR_DELAY_UI
			)
			if (!success) {
				Log.e(TAG, "Failed to register proximity sensor")
			}
		} else {
			Log.w(TAG, "Proximity sensor not available")
		}

		isRegistered = true
		Log.d(TAG, "Successfully registered sensor listeners")
	}

	fun stopMonitoring() {
		if (!isRegistered) {
			Log.d(TAG, "Sensors not registered")
			return
		}
		sensorManager.unregisterListener(sensorListener)
		isRegistered = false
		Log.d(TAG, "Unregistered sensor listeners")
	}

	private fun processOrientation() {
		if (isProcessing) return
		isProcessing = true

		val x = lastAccelReading[0]
		val y = lastAccelReading[1]
		val z = lastAccelReading[2]

		// Adjust thresholds based on sensitivity
		val gyroThreshold = 0.12f - (sensitivity * 0.08f) 
		val accelThreshold = 8.8f + ((1.0f - sensitivity) * 1.0f)

		// Check if the phone is relatively stable (not in motion)
		val isStable = abs(lastGyroReading[0]) < gyroThreshold &&
				abs(lastGyroReading[1]) < gyroThreshold &&
				abs(lastGyroReading[2]) < gyroThreshold

		// For true face-down, we also want X and Y to be near zero
		val isFlat = abs(x) < 2.5f && abs(y) < 2.5f

		// For face down, check stability. For other orientations, update immediately
		val orientation = when {
			abs(z) >= accelThreshold && z < 0 && isFlat -> {
				// Only check stability for face down
				if (isStable) PhoneOrientation.FACE_DOWN else _orientation.value
			}

			// When high sensitivity mode is enabled, any orientation that's not face down is considered face up
			else -> {
				val isHighSensitivityEnabled = if (highSensitivityScheduleEnabled) {
					highSensitivityMode && isWithinSchedule(
						highSensitivityScheduleStartTime,
						highSensitivityScheduleEndTime,
						highSensitivityScheduleDays
					)
				} else {
					highSensitivityMode
				}

				if (isHighSensitivityEnabled) {
					PhoneOrientation.FACE_UP
				} else if (abs(z) >= accelThreshold && z > 0) {
					PhoneOrientation.FACE_UP
				} else {
					_orientation.value
				}
			}
		}

		if (orientation != _orientation.value) {
			_orientation.value = orientation
		}

		isProcessing = false
	}

	private fun isWithinSchedule(startTime: String, endTime: String, days: Set<Int>): Boolean {
		try {
			val now = java.util.Calendar.getInstance()
			val dayOfWeek = now.get(java.util.Calendar.DAY_OF_WEEK)

			if (!days.contains(dayOfWeek)) {
				return false
			}

			val currentTime = String.format(
				"%02d:%02d",
				now.get(java.util.Calendar.HOUR_OF_DAY),
				now.get(java.util.Calendar.MINUTE)
			)

			return if (startTime <= endTime) {
				currentTime in startTime..endTime
			} else {
				currentTime >= startTime || currentTime <= endTime
			}
		} catch (e: Exception) {
			Log.e(TAG, "Error checking schedule: ${e.message}")
			return true // Default to true if scheduling fails
		}
	}
}
