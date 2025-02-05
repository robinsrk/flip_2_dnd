package dev.robin.flip_2_dnd.presentation.settings

import dev.robin.flip_2_dnd.R

enum class Sound(val displayName: String, val soundResId: Int) {
    SLUSH("Slush", R.raw.slush),
    HISS("Hiss", R.raw.hiss),
    SHH("Shh", R.raw.shh),
    WHISTLE("Whistle", R.raw.whistle),
    NONE("None", 0)
}