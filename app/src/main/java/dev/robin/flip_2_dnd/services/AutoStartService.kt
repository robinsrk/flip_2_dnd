package dev.robin.flip_2_dnd.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class AutoStartService : BroadcastReceiver() {

	override fun onReceive(context: Context?, intent: Intent?) {
		if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
			context?.let {
				val serviceIntent = Intent(it, FlipDetectorService::class.java)
				ContextCompat.startForegroundService(it, serviceIntent)
			}
		}
	}
}