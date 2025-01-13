package dev.robin.flip_2_dnd.services

import android.content.Context
import android.media.MediaPlayer
import dev.robin.flip_2_dnd.R

class SoundService(private val context: Context) {
	private var mediaPlayer: MediaPlayer? = null

	fun playDndSound() {
		try {
			// Release any existing MediaPlayer
			mediaPlayer?.release()

			// Create and play the new sound
			mediaPlayer = MediaPlayer.create(context, R.raw.slush)
			mediaPlayer?.setOnCompletionListener { mp ->
				mp.release()
				mediaPlayer = null
			}
			mediaPlayer?.start()
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	fun release() {
		mediaPlayer?.release()
		mediaPlayer = null
	}
}
