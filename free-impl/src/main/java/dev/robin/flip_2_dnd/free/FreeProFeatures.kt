package dev.robin.flip_2_dnd.free

import android.content.Context
import dev.robin.flip_2_dnd.core.ProFeatureManager

class FreeProFeatures(context: Context) : ProFeatureManager {
    override fun autoStartEnabled() = false
    override fun advancedSensitivityEnabled() = false
    override fun delayCustomizationEnabled() = false
    override fun scheduleEnabled() = false
    override fun customSoundEnabled() = false
    override fun batterySaverSyncEnabled() = false
    override fun detectionFiltersEnabled() = false
    override fun telegramSupportEnabled() = false
    override fun flashlightFeedbackEnabled() = false
    override fun isPro() = false
}
