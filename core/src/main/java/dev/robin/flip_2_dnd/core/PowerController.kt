package dev.robin.flip_2_dnd.core

interface PowerController {
    /**
     * Toggles the system battery saver (low power mode).
     * Requires WRITE_SECURE_SETTINGS or user prompt fallback.
     */
    fun setBatterySaverEnabled(enabled: Boolean)
}
