package dev.robin.flip_2_dnd.presentation.settings

import dev.robin.flip_2_dnd.R

enum class Sound(val stringResId: Int, val soundResId: Int, val customSoundUri: String? = null) {
    SLUSH(R.string.sound_slush, R.raw.slush),
    HISS(R.string.sound_hiss, R.raw.hiss),
    SHH(R.string.sound_shh, R.raw.shh),
    WHISTLE(R.string.sound_whistle, R.raw.whistle),
    CUSTOM(R.string.custom_sound, 0, null),
    NONE(R.string.none, 0)
}