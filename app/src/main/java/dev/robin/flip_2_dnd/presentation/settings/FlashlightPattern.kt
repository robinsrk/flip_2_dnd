package dev.robin.flip_2_dnd.presentation.settings

import dev.robin.flip_2_dnd.R

enum class FlashlightPattern(val stringResId: Int, val pattern: LongArray) {
    NONE(R.string.none, longArrayOf()),
    SINGLE_BLINK(R.string.flashlight_single_blink, longArrayOf(0, 200, 200)),
    DOUBLE_BLINK(R.string.flashlight_double_blink, longArrayOf(0, 200, 200, 200, 200)),
    TRIPLE_BLINK(R.string.flashlight_triple_blink, longArrayOf(0, 200, 100, 200, 100, 200, 200)),
    LONG_BLINK(R.string.flashlight_long_blink, longArrayOf(0, 800, 400)),
    RAPID_BLINK(R.string.flashlight_rapid_blink, longArrayOf(0, 50, 50, 50, 50, 50, 50, 50, 200))
}
