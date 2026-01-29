package dev.robin.flip_2_dnd

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FlipDndApp : Application() {
	override fun onCreate() {
		super.onCreate()
		initializePremium()
	}
}
