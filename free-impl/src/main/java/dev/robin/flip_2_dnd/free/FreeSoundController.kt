package dev.robin.flip_2_dnd.free

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import dev.robin.flip_2_dnd.core.SoundController

class FreeSoundController(private val context: android.content.Context) : SoundController {
    private val TAG = "FreeSoundController"
    private var mediaPlayer: MediaPlayer? = null

    override fun playSound(sound: dev.robin.flip_2_dnd.core.Sound, uri: String?, volume: Float, useCustomVolume: Boolean) {
        if (sound == dev.robin.flip_2_dnd.core.Sound.NONE) return

        if (sound == dev.robin.flip_2_dnd.core.Sound.CUSTOM) return // Free version does not support custom sounds

        try {
            mediaPlayer?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error playing sound: ${e.message}")
        }
        finally {
            mediaPlayer = null;
        }

        var player: MediaPlayer? = null
        try {
            // setUsage to Alarm to allow sound even if DND is active
            val attributes = android.media.AudioAttributes.Builder()
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                .build()

            player = MediaPlayer().apply {
                setAudioAttributes(attributes)
            }

            when (sound) {
                dev.robin.flip_2_dnd.core.Sound.SYSTEM_DEFAULT -> {
                    val notification = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
                    player.setDataSource(context, notification)
                }
                else -> {
                    // Here we use AssetFileDescriptor to be able to apply the attributes before preparing...
                    context.resources.openRawResourceFd(sound.soundResId).use { afd ->
                        player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    }
                }
            }

            player.prepare()

            if (useCustomVolume) {
                player.setVolume(volume, volume)
            }

            player.setOnCompletionListener {
                it.release()
                if (mediaPlayer == it) mediaPlayer = null
            }

            player.start()

        } catch (e: Exception) {
            Log.e(TAG, "Error playing sound: ${e.message}")
            // It's good practice to release the player if something went wrong during the loading process...
            player?.release()
            player = null
        }
        finally {
            mediaPlayer = player
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
