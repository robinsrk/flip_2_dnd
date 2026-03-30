package dev.robin.flip_2_dnd.services;

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.RequiresApi

@SuppressLint("AccessibilityPolicy")
public class TurnScreenOffService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent) {}

    override fun onInterrupt() {}

    override fun onServiceConnected() {}

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let { action ->
            Log.d(TAG, "Requested action: $action")
            when (action) {
                ACTION_TURN_SCREEN_OFF -> {
                    performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    companion object {
        private const val TAG = "TurnScreenOffService"
        private val ACTION_TURN_SCREEN_OFF = "${TurnScreenOffService::class.java.name}.TURN_SCREEN_OFF"

        fun turnScreenOff(context: Context) {
            if (isTurnScreenOffSupported() && isAccessibilityPermissionGranted(context)) {
                val intent = Intent(context, TurnScreenOffService::class.java).apply {
                    action = ACTION_TURN_SCREEN_OFF
                }
                Log.d(TAG, "Trying to turn screen off")
                context.startService(intent)
            }
            else {
                Log.d(TAG, "Can't turn screen off (Android SDK version too low)")
            }
        }
        fun isAccessibilityPermissionGranted(context: Context) : Boolean {
            try {
                val accessibilityEnabled = Settings.Secure.getInt(context.contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED)
                if (accessibilityEnabled == 1) {
                    val settingValue = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
                    if (settingValue != null) {
                        val serviceName = "${context.packageName}/${TurnScreenOffService::class.java.canonicalName}"
                        return settingValue.split(":").any { it.equals(serviceName, ignoreCase = true) }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            }
            return false
        }

        fun getRequestAccessibilityPermissionIntent() : Intent {
            return Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
        }

        fun isTurnScreenOffSupported() : Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
        }
    }
}