package dev.robin.flip_2_dnd.services

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.abs

private const val TAG = "SensorService"

class SensorService(context: Context) {
	private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
	private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
	private val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

	private val _orientation = MutableStateFlow("Face up")
	val orientation: StateFlow<String> = _orientation

	private val _accelerometerData = MutableStateFlow(FloatArray(3) { 0f })
	val accelerometerData: StateFlow<FloatArray> = _accelerometerData

	private val _gyroscopeData = MutableStateFlow(FloatArray(3) { 0f })
	val gyroscopeData: StateFlow<FloatArray> = _gyroscopeData

	private var lastAccelReading = FloatArray(3)
	private var lastGyroReading = FloatArray(3)
	private var isProcessing = false
	private var isRegistered = false

	init {
		if (accelerometer == null) {
			Log.e(TAG, "No accelerometer sensor found!")
		}
		if (gyroscope == null) {
			Log.e(TAG, "No gyroscope sensor found!")
		}
	}

	private val sensorListener = object : SensorEventListener {
		override fun onSensorChanged(event: SensorEvent) {
			when (event.sensor.type) {
				Sensor.TYPE_ACCELEROMETER -> {
					lastAccelReading = event.values.clone()
					_accelerometerData.value = event.values.clone()
					Log.d(TAG, "Accelerometer data: ${lastAccelReading.contentToString()}")
					processOrientation()
				}

				Sensor.TYPE_GYROSCOPE -> {
					lastGyroReading = event.values.clone()
					_gyroscopeData.value = event.values.clone()
					Log.d(TAG, "Gyroscope data: ${lastGyroReading.contentToString()}")
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

		// Check if the phone is relatively stable (not in motion)
		val isStable = abs(lastGyroReading[0]) < 0.02f &&
				abs(lastGyroReading[1]) < 0.02f &&
				abs(lastGyroReading[2]) < 0.02f

		// For face down, check stability. For other orientations, update immediately
		val orientation = when {
			abs(z) >= 9.5f && z < 0 -> {
				// Only check stability for face down
				Log.d(TAG, "z value: " + abs(z) + " " + z)
				if (isStable) "Face down" else _orientation.value
			}

			else -> "Face up"
		}

		if (orientation != _orientation.value) {
			_orientation.value = orientation
		}

		isProcessing = false
	}
}
