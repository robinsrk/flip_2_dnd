package dev.robin.flip_2_dnd.services

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import dev.robin.flip_2_dnd.core.PhoneOrientation
import dev.robin.flip_2_dnd.core.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlin.math.abs

private const val TAG = "SensorService"

class SensorService(
  private val context: Context,
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
  private var filteredAccel = FloatArray(3)
  private val alpha = 0.15f // Smoothing factor for low-pass filter
  private var isProcessing = false
  private var _isRegistered = false
  val isRegistered: Boolean get() = _isRegistered
  private var sensitivity = 0.5f
  private var highSensitivityMode = false
  private var highSensitivityScheduleEnabled = false
  private var highSensitivityScheduleStartTime = "22:00"
  private var highSensitivityScheduleEndTime = "07:00"
  private var highSensitivityScheduleDays = setOf(1, 2, 3, 4, 5, 6, 7)

  // Scope to observe settings changes from SettingsRepository
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

  init {
    // Bind high-sensitivity-related settings to local vars
    scope.launch { settingsRepository.getHighSensitivityModeEnabled().collect { highSensitivityMode = it } }
    scope.launch {
      settingsRepository.getHighSensitivityScheduleEnabled().collect { highSensitivityScheduleEnabled = it }
    }
    scope.launch {
      settingsRepository.getHighSensitivityScheduleStartTime().collect { highSensitivityScheduleStartTime = it }
    }
    scope.launch {
      settingsRepository.getHighSensitivityScheduleEndTime().collect { highSensitivityScheduleEndTime = it }
    }
    scope.launch { settingsRepository.getHighSensitivityScheduleDays().collect { highSensitivityScheduleDays = it } }
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
      }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
      Log.d(TAG, "Sensor accuracy changed: ${sensor?.name}, accuracy: $accuracy")
    }
  }

  fun startMonitoring() {
    if (_isRegistered) {
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

    _isRegistered = true
    Log.d(TAG, "Successfully registered sensor listeners")
  }

  fun stopMonitoring() {
    if (!_isRegistered) {
      Log.d(TAG, "Sensors not registered")
      return
    }
    sensorManager.unregisterListener(sensorListener)
    _isRegistered = false
    Log.d(TAG, "Unregistered sensor listeners")
  }

  private fun processOrientation() {
    if (isProcessing) return
    isProcessing = true

    val x = lastAccelReading[0]
    val y = lastAccelReading[1]
    val z = lastAccelReading[2]

    // Adjust thresholds based on sensitivity
    val sensorManagerPro = dev.robin.flip_2_dnd.core.ServiceLocator.getSensorManagerPro(context)

    val isHighSensitivityEnabled = sensorManagerPro.isHighSensitivityEnabled(
      highSensitivityMode,
      highSensitivityScheduleEnabled,
      highSensitivityScheduleStartTime,
      highSensitivityScheduleEndTime,
      highSensitivityScheduleDays
    )

    val thresholds = sensorManagerPro.getOrientationThresholds(sensitivity, isHighSensitivityEnabled)

    // Check if the phone is relatively stable (not in motion)
    val isStable = abs(lastGyroReading[0]) < thresholds.gyro &&
            abs(lastGyroReading[1]) < thresholds.gyro &&
            abs(lastGyroReading[2]) < thresholds.gyro

    // For true face-down, we also want X and Y to be near zero
    val isFlat = abs(x) < 2.5f && abs(y) < 2.5f

    // For face down, check stability. For other orientations, update immediately
    val orientation = when {
      abs(z) >= thresholds.accel && z < 0 && isFlat -> {
        // Only check stability for face down
        if (isStable) PhoneOrientation.FACE_DOWN else _orientation.value
      }

      // When high sensitivity mode is enabled, any orientation that's not face down is considered face up
      isHighSensitivityEnabled -> {
        PhoneOrientation.FACE_UP
      }

      abs(z) >= thresholds.accel && z > 0 -> {
        PhoneOrientation.FACE_UP
      }

      else -> {
        _orientation.value
      }
    }

    if (orientation != _orientation.value) {
      _orientation.value = orientation
    }

    isProcessing = false
  }

  private fun isWithinSchedule(startTime: String, endTime: String, days: Set<Int>): Boolean {
    return dev.robin.flip_2_dnd.core.ServiceLocator.getScheduleManager(context)
      .isWithinSchedule(startTime, endTime, days)
  }
}
