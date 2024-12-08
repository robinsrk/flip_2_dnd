package dev.robin.flip_2_dnd.presentation.settings

data class SettingsState(
    val isScreenOffOnly: Boolean = false,
    val isVibrationEnabled: Boolean = true,
    val isSoundEnabled: Boolean = true,
    val isPriorityDndEnabled: Boolean = false
)
