package com.cocoit.flip_2_dnd.presentation.main

import com.cocoit.flip_2_dnd.domain.model.PhoneOrientation

data class MainState(
    val orientation: PhoneOrientation = PhoneOrientation.UNKNOWN,
    val isDndEnabled: Boolean = false,
    val isScreenOffOnly: Boolean = false,
    val isVibrationEnabled: Boolean = true,
    val isSoundEnabled: Boolean = false
)
