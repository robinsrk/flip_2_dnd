package dev.robin.flip_2_dnd.data.repository

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dev.robin.flip_2_dnd.domain.model.PhoneOrientation
import dev.robin.flip_2_dnd.domain.repository.OrientationRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class OrientationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : OrientationRepository, SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val _orientation = MutableStateFlow(PhoneOrientation.UNKNOWN)

    override fun getOrientation(): Flow<PhoneOrientation> = _orientation

    override suspend fun startMonitoring() {
        sensorManager.registerListener(
            this,
            accelerometer,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override suspend fun stopMonitoring() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val z = event.values[2]
            _orientation.value = when {
                abs(z) > 8.0f && z < 0 -> PhoneOrientation.FACE_DOWN
                // abs(z) > 9.0f && z > 0 -> PhoneOrientation.FACE_UP
                else -> PhoneOrientation.FACE_UP
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this implementation
    }
}
