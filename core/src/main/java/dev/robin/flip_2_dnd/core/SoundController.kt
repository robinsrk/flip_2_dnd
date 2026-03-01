package dev.robin.flip_2_dnd.core

import android.content.Context

interface SoundController {
    fun playSound(sound: Sound, uri: String?, volume: Float, useCustomVolume: Boolean)
    fun previewSound(sound: Sound, uri: String?, volume: Float, useCustomVolume: Boolean)
    fun stopSound()
    fun release()
}
