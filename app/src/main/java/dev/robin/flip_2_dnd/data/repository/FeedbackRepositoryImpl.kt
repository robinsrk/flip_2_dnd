package dev.robin.flip_2_dnd.data.repository

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import dev.robin.flip_2_dnd.core.FeedbackRepository
import dev.robin.flip_2_dnd.core.Sound
import dev.robin.flip_2_dnd.core.SoundController
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedbackRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val soundController: SoundController
) : FeedbackRepository {

    private val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as android.os.VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    override fun vibrate() {
        val vibrationEffect = VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrator.vibrate(vibrationEffect)
    }

    override fun playSound() {
        soundController.playSound(
            sound = Sound.SYSTEM_DEFAULT,
            uri = null,
            volume = 1.0f,
            useCustomVolume = false
        )
    }
}
