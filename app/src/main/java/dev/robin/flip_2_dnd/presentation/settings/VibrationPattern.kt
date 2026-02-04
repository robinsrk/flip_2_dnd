package dev.robin.flip_2_dnd.presentation.settings

import dev.robin.flip_2_dnd.R

enum class VibrationPattern(val stringResId: Int, val pattern: LongArray) {
    NONE(R.string.none, longArrayOf()),
    SINGLE_PULSE(R.string.vibration_single_pulse, longArrayOf(0, 200, 400)),
    DOUBLE_PULSE(R.string.vibration_double_pulse, longArrayOf(0, 200, 200, 200, 400)),
    TRIPLE_PULSE(R.string.vibration_triple_pulse, longArrayOf(0, 200, 100, 200, 100, 200, 400)),
    LONG_PULSE(R.string.vibration_long_pulse, longArrayOf(0, 800, 600)),
    RAPID_PULSE(R.string.vibration_rapid_pulse, longArrayOf(0, 50, 50, 50, 50, 50, 50, 50, 400)),
    HEARTBEAT(R.string.vibration_heartbeat, longArrayOf(0, 100, 100, 300, 600)),
    TICK_TOCK(R.string.vibration_tick_tock, longArrayOf(0, 50, 400, 50, 400))
}