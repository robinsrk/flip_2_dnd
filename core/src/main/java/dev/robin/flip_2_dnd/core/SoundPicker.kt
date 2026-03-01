package dev.robin.flip_2_dnd.core

import android.content.Context

interface SoundPicker {
    fun launchPicker(context: Context, isDndOn: Boolean)
}
