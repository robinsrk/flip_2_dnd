package dev.robin.flip_2_dnd

class FreePremiumEngine : PremiumEngine {
	override fun autoStartEnabled() = false
	override fun advancedSensitivityEnabled() = false
	override fun delayCustomizationEnabled() = false
	override fun scheduleEnabled() = false
	override fun customSoundEnabled() = false
	override fun batterySaverSyncEnabled() = false
	override fun detectionFiltersEnabled() = false
	override fun telegramSupportEnabled() = false
}
