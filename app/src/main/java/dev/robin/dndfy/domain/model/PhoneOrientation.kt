package dev.robin.dndfy.domain.model

enum class PhoneOrientation {
    FACE_UP,
    FACE_DOWN,
    FACE_RIGHT,
    FACE_LEFT,
    UNKNOWN;

    companion object {
        fun fromString(value: String): PhoneOrientation = when (value.lowercase()) {
            "face up" -> FACE_UP
            "face down" -> FACE_DOWN
            "face right" -> FACE_RIGHT
            "face left" -> FACE_LEFT
            else -> UNKNOWN
        }
    }

    override fun toString(): String = when (this) {
        FACE_UP -> "Face up"
        FACE_DOWN -> "Face down"
        FACE_RIGHT -> "Face right"
        FACE_LEFT -> "Face left"
        UNKNOWN -> "Unknown"
    }
}
