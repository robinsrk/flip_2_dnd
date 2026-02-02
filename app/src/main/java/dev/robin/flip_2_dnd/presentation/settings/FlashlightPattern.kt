package dev.robin.flip_2_dnd.presentation.settings

enum class FlashlightPattern(val displayName: String, val pattern: LongArray) {
    NONE("None", longArrayOf()),
    SINGLE_BLINK("Single Blink", longArrayOf(0, 200, 200)),
    DOUBLE_BLINK("Double Blink", longArrayOf(0, 200, 200, 200, 200)),
    TRIPLE_BLINK("Triple Blink", longArrayOf(0, 200, 100, 200, 100, 200, 200)),
    LONG_BLINK("Long Blink", longArrayOf(0, 800, 400)),
    RAPID_BLINK("Rapid Blink", longArrayOf(0, 50, 50, 50, 50, 50, 50, 50, 200))
}
