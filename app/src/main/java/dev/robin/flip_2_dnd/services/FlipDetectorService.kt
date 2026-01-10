package dev.robin.flip_2_dnd.services

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import dev.robin.flip_2_dnd.MainActivity
import dev.robin.flip_2_dnd.R
import dev.robin.flip_2_dnd.domain.repository.SettingsRepository
import dev.robin.flip_2_dnd.data.repository.SettingsRepositoryImpl
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.first

private const val TAG = "FlipDetectorService"
private const val SERVICE_NOTIFICATION_ID = 1
private const val FLIP_NOTIFICATION_ID = 2
private const val DND_NOTIFICATION_ID = 3
private const val SERVICE_CHANNEL_ID = "FlipDetectorChannel"
private const val FLIP_CHANNEL_ID = "FlipStateChannel"

class FlipDetectorService : Service() {
    lateinit var sensorService: SensorService
        private set
    private lateinit var dndService: DndService
    private lateinit var powerManager: PowerManager
    private lateinit var wakeLock: PowerManager.WakeLock
    private lateinit var settingsRepository: SettingsRepository
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private val _isScreenOff = MutableStateFlow(false)
    private val isScreenOff = _isScreenOff.asStateFlow()
    private var onlyWhenScreenOff = false
    private var activationDelaySeconds = 0
    private var orientationJob: Job? = null

    private val screenStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    Log.d(TAG, "Screen turned OFF")
                    _isScreenOff.value = true
                    // Ensure wake lock is held when screen is off
                    acquireWakeLock()
                }
                Intent.ACTION_SCREEN_ON -> {
                    Log.d(TAG, "Screen turned ON")
                    _isScreenOff.value = false
                    // Release wake lock when screen is on to save battery
                    releaseWakeLock()
                }
                Intent.ACTION_SHUTDOWN -> {
                    Log.d(TAG, "System shutdown - Stopping sensor monitoring")
                    sensorService.stopMonitoring()
                    releaseWakeLock()
                }
                Intent.ACTION_REBOOT -> {
                    Log.d(TAG, "System reboot - Stopping sensor monitoring")
                    sensorService.stopMonitoring()
                    releaseWakeLock()
                }
            }
        }
    }

    private fun acquireWakeLock() {
        try {
            if (!::wakeLock.isInitialized) {
                wakeLock = powerManager.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "Flip2DND::ServiceWakeLock"
                ).apply {
                    setReferenceCounted(false)
                }
            }
            if (!wakeLock.isHeld) {
                wakeLock.acquire(10*60*1000L) // 10 minutes timeout
                Log.d(TAG, "Wake lock acquired")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error acquiring wake lock: ${e.message}")
        }
    }

    private fun releaseWakeLock() {
        try {
            if (::wakeLock.isInitialized && wakeLock.isHeld) {
                wakeLock.release()
                Log.d(TAG, "Wake lock released")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing wake lock: ${e.message}")
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: Starting FlipDetectorService")
        
        try {
            sensorService = SensorService(this)
            dndService = DndService(this)
            settingsRepository = SettingsRepositoryImpl(this)
            powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            
            // Initialize screen state
            _isScreenOff.value = !powerManager.isInteractive
            Log.d(TAG, "Initial screen state: ${if (_isScreenOff.value) "OFF" else "ON"}")
            
            // Make service high priority
            val notification = createServiceNotification()
            startForeground(SERVICE_NOTIFICATION_ID, notification)
            
            // Create notification channels
            createNotificationChannels()
            
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
            
            // Initial wake lock acquisition
            if (_isScreenOff.value) {
                acquireWakeLock()
            }
            
            // Observe settings changes
            serviceScope.launch {
                settingsRepository.getScreenOffOnlyEnabled().collect { enabled ->
                    onlyWhenScreenOff = enabled
                    Log.d(TAG, "Settings updated - Only when screen off: $enabled")
                }
            }

            serviceScope.launch {
                settingsRepository.getActivationDelay().collect { delay ->
                    activationDelaySeconds = delay
                    Log.d(TAG, "Settings updated - Activation delay: $delay seconds")
                }
            }
            
            // Observe orientation changes
            serviceScope.launch {
                combine(
                    sensorService.orientation,
                    isScreenOff
                ) { orientation, screenOff ->
                    Pair(orientation, screenOff)
                }.collect { (orientation, screenOff) ->
                    handleOrientationChange(orientation, screenOff)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.localizedMessage}", e)
            stopSelf()
        }
    }

    private fun handleOrientationChange(orientation: String, screenOff: Boolean) {
        try {
            Log.d(TAG, "Screen state: ${if (screenOff) "OFF" else "ON"}, Only when screen off: $onlyWhenScreenOff")
            Log.d(TAG, "Handling orientation change: $orientation")
            
            // Cancel any existing orientation job
            orientationJob?.cancel()
            
            // Update DND status before making any changes
            dndService.updateDndStatus()
            val currentDndState = dndService.isDndEnabled.value
            val isAppEnabled = dndService.isAppEnabledDnd.value
            Log.d(TAG, "Current DND state: enabled=$currentDndState, appEnabled=$isAppEnabled")
            
            when (orientation) {
                "Face down" -> {
                    if (!currentDndState) {
                        // Only check screen off and stability when turning ON DND
                        if (onlyWhenScreenOff && !screenOff) {
                            Log.d(TAG, "Screen is ON and 'Only when screen off' is enabled - Ignoring face down")
                            return
                        }
                        
                        Log.d(TAG, "Phone is face down and DND is OFF - Starting $activationDelaySeconds-second delay")
                        showFlipDetectedNotification()
                        orientationJob = serviceScope.launch {
                            delay(activationDelaySeconds * 1000L)
                            // Double check screen state and orientation after delay
                            if (onlyWhenScreenOff && !isScreenOff.value) {
                                Log.d(TAG, "Screen turned ON during delay - Cancelling DND toggle")
                                return@launch
                            }
                            if (sensorService.orientation.value == "Face down") {
                                Log.d(TAG, "$activationDelaySeconds-second delay completed, phone still face down - Enabling DND")
                                dndService.toggleDnd()
                                showDndStateNotification(true)

                                // Handle Battery Saver
                                try {
                                    val enableBatterySaver = settingsRepository.getBatterySaverOnFlipEnabled().first()
                                    if (enableBatterySaver) {
                                        setBatterySaverEnabled(true)
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error handling battery saver: ${e.message}", e)
                                }
                            } else {
                                Log.d(TAG, "Phone orientation changed during delay - Cancelling DND toggle")
                            }
                        }
                    } else {
                        Log.d(TAG, "Phone is face down but DND is already ON - No action needed")
                    }
                }
                else -> {
                    Log.d(TAG, "Phone is not face down: currentDndState=$currentDndState, isAppEnabled=$isAppEnabled")
                    if (currentDndState && isAppEnabled) {
                        Log.d(TAG, "Phone is not face down ($orientation) and DND was enabled by app - Disabling DND")
                        dndService.toggleDnd()
                        showDndStateNotification(false)

                        // Disable battery saver when flipping back up
                        serviceScope.launch {
                            try {
                                val enableBatterySaver = settingsRepository.getBatterySaverOnFlipEnabled().first()
                                if (enableBatterySaver) {
                                    setBatterySaverEnabled(false)
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error disabling battery saver: ${e.message}", e)
                            }
                        }
                    } else if (currentDndState) {
                        Log.d(TAG, "Phone is not face down ($orientation) but DND was enabled by user - No action needed")
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
            // Unregister screen state receiver
            unregisterReceiver(screenStateReceiver)
            
            // Stop sensor monitoring
            sensorService.stopMonitoring()
            
            // Cancel any pending orientation job
            orientationJob?.cancel()
            
            // Cancel all coroutines
            serviceScope.cancel()
            
            Log.d(TAG, "Service cleanup completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error during service cleanup: ${e.localizedMessage}", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannels() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Service channel
        val serviceChannel = NotificationChannel(
            SERVICE_CHANNEL_ID,
            "Flip 2 DND Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Monitors phone orientation to toggle DND mode"
        }

        // Flip state channel
        val flipChannel = NotificationChannel(
            FLIP_CHANNEL_ID,
            "Flip State Notifications",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Notifications about phone flip state and DND changes"
            setSound(null, null)
            enableVibration(false)
            enableLights(false)
        }

        notificationManager.createNotificationChannels(listOf(serviceChannel, flipChannel))
    }

    private fun showFlipDetectedNotification() {
        // Use runBlocking to get the current notification preference synchronously
        serviceScope.launch {
            val notificationsEnabled = settingsRepository.getNotificationsEnabled().first()
            if (!notificationsEnabled) {
                Log.d(TAG, "Notifications disabled in settings - skipping flip notification")
                return@launch
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationText = if (activationDelaySeconds > 0) {
                getString(R.string.seconds, activationDelaySeconds).let { "DND will activate in $it" }
            } else {
                "DND will activate now"
            }
            val notification = NotificationCompat.Builder(this@FlipDetectorService, FLIP_CHANNEL_ID)
                .setContentTitle("Flip Detected")
                .setContentText(notificationText)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSilent(true) // Make notification silent
                .build()

            notificationManager.notify(FLIP_NOTIFICATION_ID, notification)
            Log.d(TAG, "Flip detected notification shown")
        }
    }

    private fun showDndStateNotification(enabled: Boolean) {
        // Use runBlocking to get the current notification preference synchronously
        serviceScope.launch {
            val notificationsEnabled = settingsRepository.getNotificationsEnabled().first()
            if (!notificationsEnabled) {
                Log.d(TAG, "Notifications disabled in settings - skipping DND state notification")
                return@launch
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notification = NotificationCompat.Builder(this@FlipDetectorService, FLIP_CHANNEL_ID)
                .setContentTitle("DND State Changed")
                .setContentText(if (enabled) "DND mode activated" else "DND mode deactivated")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSilent(true) // Make notification silent
                .build()

            notificationManager.notify(DND_NOTIFICATION_ID, notification)
            Log.d(TAG, "DND state notification shown: DND ${if (enabled) "enabled" else "disabled"}")
        }
    }

    private fun setBatterySaverEnabled(enabled: Boolean) {
        try {
            val isPowerSaveMode = powerManager.isPowerSaveMode
            if (enabled == isPowerSaveMode) {
                Log.d(TAG, "Battery Saver is already ${if (enabled) "enabled" else "disabled"}")
                return
            }

            // Attempt to toggle battery saver directly
            // This requires WRITE_SECURE_SETTINGS permission granted via ADB
            val value = if (enabled) 1 else 0
            val success = Settings.Global.putInt(contentResolver, "low_power", value)
            
            if (success) {
                Log.d(TAG, "Battery Saver ${if (enabled) "enabled" else "disabled"} via Secure Settings")
            } else {
                Log.w(TAG, "Failed to set Battery Saver via Secure Settings - Falling back to prompt")
                if (enabled) promptEnableBatterySaver()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling Battery Saver: ${e.message}")
            // Fallback to notification prompt if we're trying to enable it
            if (enabled) promptEnableBatterySaver()
        }
    }

    private fun promptEnableBatterySaver() {
        try {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val intent = Intent(android.provider.Settings.ACTION_BATTERY_SAVER_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val pendingIntent = PendingIntent.getActivity(
                this, 100,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(this@FlipDetectorService, FLIP_CHANNEL_ID)
                .setContentTitle("Battery Saver suggested")
                .setContentText("Tap to enable Battery Saver")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setSilent(true)
                .build()

            notificationManager.notify(FLIP_NOTIFICATION_ID + 100, notification)
            Log.d(TAG, "Battery Saver prompt notification shown")
        } catch (e: Exception) {
            Log.e(TAG, "Error prompting Battery Saver: ${e.message}", e)
        }
    }

    private fun createServiceNotification(): Notification {
        val channelName = "Flip 2 DND Service"
        val channel = NotificationChannel(
            SERVICE_CHANNEL_ID,
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

        return NotificationCompat.Builder(this, SERVICE_CHANNEL_ID)
            .setContentTitle("Flip 2 DND")
            .setContentText("Monitoring phone orientation")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .build()
    }
}
