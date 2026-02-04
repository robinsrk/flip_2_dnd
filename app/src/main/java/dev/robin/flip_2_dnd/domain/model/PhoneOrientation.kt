package dev.robin.flip_2_dnd.domain.model

import dev.robin.flip_2_dnd.R

enum class PhoneOrientation(val stringResId: Int) {
    FACE_UP(R.string.orientation_face_up),
    FACE_DOWN(R.string.orientation_face_down),
    UNKNOWN(R.string.unknown);

    companion object {
        fun fromString(value: String): PhoneOrientation = when (value.lowercase()) {
            "face up" -> FACE_UP
            "face down" -> FACE_DOWN
            else -> UNKNOWN
        }
    }

    override fun toString(): String = name
}
