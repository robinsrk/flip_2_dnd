package dev.robin.flip_2_dnd.core

interface FlashController {
    /**
     * Flashes the flashlight with the given pattern and intensity.
     * @param pattern The pattern of timings (ON/OFF).
     * @param intensity The intensity level (1-10).
     */
    fun flashFlashlight(pattern: LongArray, intensity: Int)
    
    /**
     * Checks if flashlight feedback should be skipped based on current state.
     */
    fun shouldSkipFeedback(isFlashlightOn: Boolean, feedbackWithFlashlightOn: Boolean): Boolean
}
