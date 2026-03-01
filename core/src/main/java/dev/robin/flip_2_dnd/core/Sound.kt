package dev.robin.flip_2_dnd.core

enum class Sound(val stringResId: Int, val soundResId: Int, val customSoundUri: String? = null) {
    SLUSH(R.string.sound_slush, R.raw.slush),
    HISS(R.string.sound_hiss, R.raw.hiss),
    SHH(R.string.sound_shh, R.raw.shh),
    WHISTLE(R.string.sound_whistle, R.raw.whistle),
    SYSTEM_DEFAULT(R.string.none, 0), // Uses none as placeholder strings
    CUSTOM(R.string.custom_sound, 0, null),
    NONE(R.string.none, 0)
}
