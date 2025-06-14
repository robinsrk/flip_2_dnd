package dev.robin.dndfy.presentation.settings

import dev.robin.dndfy.R

enum class Sound(val displayName: String, val soundResId: Int, val customSoundUri: String? = null) {
    SLUSH("Slush", R.raw.slush),
    HISS("Hiss", R.raw.hiss),
    SHH("Shh", R.raw.shh),
    WHISTLE("Whistle", R.raw.whistle),
    CUSTOM("Custom", 0, null),
    NONE("None", 0)
}