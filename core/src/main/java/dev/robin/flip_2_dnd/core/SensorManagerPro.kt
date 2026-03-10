package dev.robin.flip_2_dnd.core

interface SensorManagerPro {
    /**
     * Returns orientation thresholds based on the given sensitivity.
     * High sensitivity is assumed to be always-on.
     */
    fun getOrientationThresholds(sensitivity: Float): Thresholds

    data class Thresholds(val gyro: Float, val accel: Float)
}
