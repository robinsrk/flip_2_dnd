package dev.robin.dndfy.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class AutoStartService : BroadcastReceiver() {

	override fun onReceive(context: Context?, intent: Intent?) {
		if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
			context?.let {
				val serviceIntent = Intent(it, DNDfyDetectorService::class.java)
				ContextCompat.startForegroundService(it, serviceIntent)
			}
		}
	}
}