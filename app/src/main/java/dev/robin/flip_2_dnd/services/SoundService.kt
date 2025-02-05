package dev.robin.flip_2_dnd.services

import android.content.Context
import android.media.MediaPlayer
import dev.robin.flip_2_dnd.domain.repository.SettingsRepository
import dev.robin.flip_2_dnd.data.repository.SettingsRepositoryImpl
import dev.robin.flip_2_dnd.presentation.settings.Sound
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class SoundService(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private val settingsRepository: SettingsRepository = SettingsRepositoryImpl(context)

    fun playDndSound(isEnabled: Boolean) {
        runBlocking {
            val isSoundEnabled = settingsRepository.getSoundEnabled().first()
            if (!isSoundEnabled) return@runBlocking

            try {
                // Release any existing MediaPlayer
                mediaPlayer?.release()

                // Get the appropriate sound based on DND state
                val sound = if (isEnabled) {
                    settingsRepository.getDndOnSound().first()
                } else {
                    settingsRepository.getDndOffSound().first()
                }

                // Don't play if NONE is selected
                if (sound == Sound.NONE) return@runBlocking

                // Create and play the new sound
                mediaPlayer = MediaPlayer.create(context, sound.soundResId)
                
                // Set custom volume if enabled
                if (settingsRepository.getUseCustomVolume().first()) {
                    val volume = settingsRepository.getCustomVolume().first()
                    mediaPlayer?.setVolume(volume, volume)
                }

                mediaPlayer?.setOnCompletionListener { mp ->
                    mp.release()
                    mediaPlayer = null
                }
                mediaPlayer?.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
