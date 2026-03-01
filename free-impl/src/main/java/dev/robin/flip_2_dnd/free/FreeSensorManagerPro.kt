package dev.robin.flip_2_dnd.free

import android.content.Context
import dev.robin.flip_2_dnd.core.SensorManagerPro

class FreeSensorManagerPro(context: Context) : SensorManagerPro {
    override fun getOrientationThresholds(sensitivity: Float, isHighSensitivityEnabled: Boolean): SensorManagerPro.Thresholds {
        // Default thresholds for Free version
        return SensorManagerPro.Thresholds(gyro = 0.12f - (sensitivity * 0.08f), accel = 8.8f + ((1.0f - sensitivity) * 1.0f))
    }

    override fun isHighSensitivityEnabled(
        mode: Boolean,
        scheduleEnabled: Boolean,
        startTime: String,
        endTime: String,
        days: Set<Int>
    ): Boolean {
        // High sensitivity is a Pro feature, always return false in Free version
        return false
    }
}
