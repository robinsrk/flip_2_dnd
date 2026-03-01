package dev.robin.flip_2_dnd.core

interface SensorManagerPro {
    /**
     * Determines the orientation threshold based on sensitivity and high-sensitivity mode.
     */
    fun getOrientationThresholds(sensitivity: Float, isHighSensitivityEnabled: Boolean): Thresholds

    /**
     * Checks if high sensitivity mode should be active based on mode and schedule.
     */
    fun isHighSensitivityEnabled(
        mode: Boolean,
        scheduleEnabled: Boolean,
        startTime: String,
        endTime: String,
        days: Set<Int>
    ): Boolean

    data class Thresholds(val gyro: Float, val accel: Float)
}
