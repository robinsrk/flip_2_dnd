package dev.robin.flip_2_dnd.presentation.main

import dev.robin.flip_2_dnd.domain.model.PhoneOrientation

data class MainState(
    val orientation: PhoneOrientation = PhoneOrientation.UNKNOWN,
    val isDndEnabled: Boolean = false,
    val dndMode: String = "All Notifications",
    val isScreenOffOnly: Boolean = false,
    val isVibrationEnabled: Boolean = true,
    val isSoundEnabled: Boolean = false,
    val isServiceRunning: Boolean = true
)
