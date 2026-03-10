package dev.robin.flip_2_dnd.free

import android.content.Context
import dev.robin.flip_2_dnd.core.SensorManagerPro

class FreeSensorManagerPro(context: Context) : SensorManagerPro {
    override fun getOrientationThresholds(sensitivity: Float): SensorManagerPro.Thresholds {
        // Default thresholds for Free version; high sensitivity considered always on
        val gyro = 0.12f - (sensitivity * 0.08f)
        val accel = 8.8f + ((1.0f - sensitivity) * 1.0f)
        return SensorManagerPro.Thresholds(gyro, accel)
    }
}
