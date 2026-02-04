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

	private val _orientation = MutableStateFlow(PhoneOrientation.FACE_UP)
	val orientation: StateFlow<PhoneOrientation> = _orientation

	private val _accelerometerData = MutableStateFlow(FloatArray(3) { 0f })
	val accelerometerData: StateFlow<FloatArray> = _accelerometerData

	private val _gyroscopeData = MutableStateFlow(FloatArray(3) { 0f })
	val gyroscopeData: StateFlow<FloatArray> = _gyroscopeData

	private var lastAccelReading = FloatArray(3)
	private var lastGyroReading = FloatArray(3)
	private var isProcessing = false
	private var isRegistered = false
	private var sensitivity = 0.5f
	private var highSensitivityMode = false

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
	}

	private val sensorListener = object : SensorEventListener {
		override fun onSensorChanged(event: SensorEvent) {
			when (event.sensor.type) {
				Sensor.TYPE_ACCELEROMETER -> {
					lastAccelReading = event.values.clone()
					_accelerometerData.value = event.values.clone()
//					Log.d(TAG, "Accelerometer data: ${lastAccelReading.contentToString()}")
					processOrientation()
				}

				Sensor.TYPE_GYROSCOPE -> {
					lastGyroReading = event.values.clone()
					_gyroscopeData.value = event.values.clone()
//					Log.d(TAG, "Gyroscope data: ${lastGyroReading.contentToString()}")
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
			SensorManager.SENSOR_DELAY_UI
		)
		if (!success) {
			Log.e(TAG, "Failed to register accelerometer")
			return
		}

		success = success && sensorManager.registerListener(
			sensorListener,
			gyroscope,
			SensorManager.SENSOR_DELAY_UI
		)
		if (!success) {
			Log.e(TAG, "Failed to register gyroscope")
			sensorManager.unregisterListener(sensorListener)
			return
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
		// Lower sensitivity means stricter thresholds
		val gyroThreshold = 0.1f - (sensitivity * 0.08f) // Range: 0.02f to 0.1f
		val accelThreshold = 9.0f + ((1.0f - sensitivity) * 1.0f) // Range: 9.0f to 10.0f

		// Check if the phone is relatively stable (not in motion)
		val isStable = abs(lastGyroReading[0]) < gyroThreshold &&
				abs(lastGyroReading[1]) < gyroThreshold &&
				abs(lastGyroReading[2]) < gyroThreshold

		// For face down, check stability. For other orientations, update immediately
		val orientation = when {
			abs(z) >= accelThreshold && z < 0 -> {
				// Only check stability for face down
				Log.d(TAG, "z value: ${abs(z)} $z (threshold: $accelThreshold, stable: $isStable)")
				if (isStable) PhoneOrientation.FACE_DOWN else _orientation.value
			}

			// When high sensitivity mode is enabled, any orientation that's not face down is considered face up
			else -> {
				if (highSensitivityMode) {
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
}
