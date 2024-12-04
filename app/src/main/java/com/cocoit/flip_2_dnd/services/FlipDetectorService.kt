package com.cocoit.flip_2_dnd.services

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.cocoit.flip_2_dnd.MainActivity
import com.cocoit.flip_2_dnd.R
import com.cocoit.flip_2_dnd.settings.SettingsManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

private const val TAG = "FlipDetectorService"
private const val NOTIFICATION_ID = 1
private const val CHANNEL_ID = "FlipDetectorChannel"

class FlipDetectorService : Service() {
    lateinit var sensorService: SensorService
        private set
    private lateinit var dndService: DndService
    private lateinit var powerManager: PowerManager
    private lateinit var settingsManager: SettingsManager
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private var isScreenOff = false
    private var onlyWhenScreenOff = false

    private val screenStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    Log.d(TAG, "Screen turned OFF")
                    isScreenOff = true
                }
                Intent.ACTION_SCREEN_ON -> {
                    Log.d(TAG, "Screen turned ON")
                    isScreenOff = false
                }
                Intent.ACTION_SHUTDOWN -> {
                    Log.d(TAG, "System shutdown - Stopping sensor monitoring")
                    sensorService.stopMonitoring()
                }
                Intent.ACTION_REBOOT -> {
                    Log.d(TAG, "System reboot - Stopping sensor monitoring")
                    sensorService.stopMonitoring()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: Starting FlipDetectorService")
        
        try {
            sensorService = SensorService(this)
            dndService = DndService(this)
            settingsManager = SettingsManager(this)
            powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            
            // Initialize screen state
            isScreenOff = !powerManager.isInteractive
            
            // Make service high priority
            val notification = createNotification()
            startForeground(NOTIFICATION_ID, notification)
            
            // Register screen state receiver
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_SHUTDOWN)
                addAction(Intent.ACTION_REBOOT)
            }
            registerReceiver(screenStateReceiver, filter)
            
            // Start sensor monitoring regardless of screen state
            Log.d(TAG, "Starting initial sensor monitoring")
            sensorService.startMonitoring()
            
            // Acquire wake lock to keep service running
            val wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "Flip2DND::ServiceWakeLock"
            )
            wakeLock.acquire()
            
            // Observe settings changes
            serviceScope.launch {
                settingsManager.onlyWhenScreenOff.collect { enabled ->
                    onlyWhenScreenOff = enabled
                    Log.d(TAG, "Settings updated - Only when screen off: $enabled")
                }
            }
            
            // Observe orientation changes
            serviceScope.launch {
                sensorService.orientation.collect { orientation ->
                    handleOrientationChange(orientation)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.localizedMessage}", e)
            stopSelf()
        }
    }

    private fun handleOrientationChange(orientation: String) {
        try {
            Log.d(TAG, "Screen state: ${if (isScreenOff) "OFF" else "ON"}, Only when screen off: $onlyWhenScreenOff")
            
            // If "Only when screen off" is enabled, check screen state
            if (onlyWhenScreenOff) {
                if (!isScreenOff) {
                    Log.d(TAG, "Screen is ON and 'Only when screen off' is enabled - Ignoring orientation change")
                    return
                }
                Log.d(TAG, "Screen is OFF and 'Only when screen off' is enabled - Processing orientation change")
            } else {
                Log.d(TAG, "'Only when screen off' is disabled - Processing orientation change")
            }
            
            Log.d(TAG, "Handling orientation change: $orientation")
            
            // Update DND status before making any changes
            dndService.updateDndStatus()
            val currentDndState = dndService.isDndEnabled.value
            
            when (orientation) {
                "Face down" -> {
                    if (!currentDndState) {
                        Log.d(TAG, "Phone is face down and DND is OFF - Enabling DND")
                        dndService.toggleDnd()
                    } else {
                        Log.d(TAG, "Phone is face down but DND is already ON - No action needed")
                    }
                }
                else -> {
                    if (currentDndState) {
                        Log.d(TAG, "Phone is not face down ($orientation) and DND is ON - Disabling DND")
                        dndService.toggleDnd()
                    } else {
                        Log.d(TAG, "Phone is not face down ($orientation) and DND is already OFF - No action needed")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling orientation change: ${e.localizedMessage}", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: Cleaning up FlipDetectorService")
        try {
            unregisterReceiver(screenStateReceiver)
            serviceScope.cancel()
            sensorService.stopMonitoring()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onDestroy: ${e.localizedMessage}", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        val channelName = "Flip 2 DND Service"
        val channel = NotificationChannel(
            CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Monitors phone orientation to toggle DND mode"
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        val pendingIntent = Intent(this, MainActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(
                this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Flip 2 DND")
            .setContentText("Monitoring phone orientation")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()
    }
}
