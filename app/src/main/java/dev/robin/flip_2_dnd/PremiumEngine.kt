package dev.robin.flip_2_dnd

interface PremiumEngine {
	fun autoStartEnabled(): Boolean
	fun advancedSensitivityEnabled(): Boolean
	fun delayCustomizationEnabled(): Boolean
	fun scheduleEnabled(): Boolean
	fun customSoundEnabled(): Boolean
	fun batterySaverSyncEnabled(): Boolean
	fun detectionFiltersEnabled(): Boolean
	fun telegramSupportEnabled(): Boolean
	fun flashlightFeedbackEnabled(): Boolean
}
