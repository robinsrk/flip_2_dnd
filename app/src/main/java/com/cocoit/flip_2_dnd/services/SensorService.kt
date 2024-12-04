package com.cocoit.flip_2_dnd.services

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
            Log.e(TAG, "Required sensors not available - Accelerometer: ${accelerometer != null}, Gyroscope: ${gyroscope != null}")
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
        val isStable = abs(lastGyroReading[0]) < 0.3f && 
                       abs(lastGyroReading[1]) < 0.3f && 
                       abs(lastGyroReading[2]) < 0.3f

        if (isStable) {
            val orientation = when {
                abs(z) > 8.0f && z < 0 -> "Face down"
                abs(z) > 8.0f && z > 0 -> "Face up"
                abs(x) > 8.0f && x < 0 -> "Left side"
                abs(x) > 8.0f && x > 0 -> "Right side"
                abs(y) > 8.0f && y < 0 -> "Portrait"
                abs(y) > 8.0f && y > 0 -> "Portrait reverse"
                else -> _orientation.value
            }
            
            if (orientation != _orientation.value) {
                Log.d(TAG, "Orientation changed from ${_orientation.value} to $orientation")
                _orientation.value = orientation
            }
        }

        isProcessing = false
    }
}
