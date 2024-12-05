package dev.robin.flip_2_dnd.domain.model

enum class PhoneOrientation {
    FACE_UP,
    FACE_DOWN,
    UNKNOWN;

    companion object {
        fun fromString(value: String): PhoneOrientation = when (value.lowercase()) {
            "face up" -> FACE_UP
            "face down" -> FACE_DOWN
            else -> UNKNOWN
        }
    }

    override fun toString(): String = when (this) {
        FACE_UP -> "Face up"
        FACE_DOWN -> "Face down"
        UNKNOWN -> "Unknown"
    }
}
