package dev.robin.flip_2_dnd.presentation.settings

enum class VibrationPattern(val displayName: String, val pattern: LongArray) {
    NONE("None", longArrayOf()),
    SINGLE_PULSE("Single Pulse", longArrayOf(0, 200, 400)),
    DOUBLE_PULSE("Double Pulse", longArrayOf(0, 200, 200, 200, 400)),
    TRIPLE_PULSE("Triple Pulse", longArrayOf(0, 200, 100, 200, 100, 200, 400)),
    LONG_PULSE("Long Pulse", longArrayOf(0, 800, 600)),
    RAPID_PULSE("Rapid Pulse", longArrayOf(0, 50, 50, 50, 50, 50, 50, 50, 400)),
    HEARTBEAT("Heartbeat", longArrayOf(0, 100, 100, 300, 600)),
    TICK_TOCK("Tick Tock", longArrayOf(0, 50, 400, 50, 400))
}