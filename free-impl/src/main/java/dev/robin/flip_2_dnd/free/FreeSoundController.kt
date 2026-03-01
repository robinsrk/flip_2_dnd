package dev.robin.flip_2_dnd.free

import android.content.Context
import dev.robin.flip_2_dnd.core.SoundController

class FreeSoundController(private val context: android.content.Context) : SoundController {
    private var mediaPlayer: android.media.MediaPlayer? = null

    override fun playSound(sound: dev.robin.flip_2_dnd.core.Sound, uri: String?, volume: Float, useCustomVolume: Boolean) {
        if (sound == dev.robin.flip_2_dnd.core.Sound.NONE) return
        
        try {
            mediaPlayer?.release()
            
            val player = if (sound == dev.robin.flip_2_dnd.core.Sound.SYSTEM_DEFAULT) {
                val notification = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
                val p = android.media.MediaPlayer().apply {
                    setAudioAttributes(
                        android.media.AudioAttributes.Builder()
                            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                            .build()
                    )
                    setDataSource(context, notification)
                    prepare()
                }
                p
            } else if (sound == dev.robin.flip_2_dnd.core.Sound.CUSTOM) {
                return // Free version does not support custom sounds
            } else {
                android.media.MediaPlayer.create(context, sound.soundResId)
            }

            if (useCustomVolume) {
                player?.setVolume(volume, volume)
            }
            player?.setOnCompletionListener { it.release(); if (mediaPlayer == it) mediaPlayer = null }
            player?.start()
            mediaPlayer = player
        } catch (e: Exception) {
            android.util.Log.e("FreeSoundController", "Error playing sound: ${e.message}")
        }
    }

    override fun previewSound(sound: dev.robin.flip_2_dnd.core.Sound, uri: String?, volume: Float, useCustomVolume: Boolean) {
        playSound(sound, uri, volume, useCustomVolume)
    }

    override fun stopSound() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun release() {
        stopSound()
    }
}
