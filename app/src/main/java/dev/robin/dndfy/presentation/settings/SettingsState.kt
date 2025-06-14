package dev.robin.dndfy.presentation.settings

data class SettingsState(
    val isScreenOffOnly: Boolean = true,
    val isVibrationEnabled: Boolean = true,
    val isSoundEnabled: Boolean = true,
    val isPriorityDndEnabled: Boolean = true
)
