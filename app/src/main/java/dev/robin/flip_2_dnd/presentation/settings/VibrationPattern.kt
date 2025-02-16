package dev.robin.flip_2_dnd.presentation.settings

enum class VibrationPattern(val displayName: String, val pattern: LongArray) {
    SINGLE_PULSE("Single Pulse", longArrayOf(0, 200, 400)),
    DOUBLE_PULSE("Double Pulse", longArrayOf(0, 200, 200, 200, 400)),
    LONG_PULSE("Long Pulse", longArrayOf(0, 800, 600))
}