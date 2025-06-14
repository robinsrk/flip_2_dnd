package dev.robin.dndfy.presentation.settings

enum class VibrationPattern(val displayName: String, val pattern: LongArray) {
    NONE("None", longArrayOf()),
    SINGLE_PULSE("Single Pulse", longArrayOf(0, 200, 400)),
    DOUBLE_PULSE("Double Pulse", longArrayOf(0, 200, 200, 200, 400)),
    LONG_PULSE("Long Pulse", longArrayOf(0, 800, 600))
}