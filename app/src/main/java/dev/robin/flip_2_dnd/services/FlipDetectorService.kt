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
import dagger.hilt.android.AndroidEntryPoint
import dev.robin.flip_2_dnd.MainActivity
import dev.robin.flip_2_dnd.R
import dev.robin.flip_2_dnd.core.PhoneOrientation
import dev.robin.flip_2_dnd.core.DndRepository
import dev.robin.flip_2_dnd.core.SettingsRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

private const val TAG = "FlipDetectorService"
private const val SERVICE_NOTIFICATION_ID = 1
private const val FLIP_NOTIFICATION_ID = 2
private const val DND_NOTIFICATION_ID = 3
private const val SERVICE_CHANNEL_ID = "FlipDetectorChannel"
private const val FLIP_CHANNEL_ID = "FlipStateChannel"

@AndroidEntryPoint
class FlipDetectorService : Service() {
    lateinit var sensorService: SensorService
        private set
    private lateinit var dndService: DndService
    private lateinit var powerManager: PowerManager
    private lateinit var wakeLock: PowerManager.WakeLock
    
    @Inject
    lateinit var settingsRepository: SettingsRepository
    
    @Inject
    lateinit var dndRepository: DndRepository
    
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private val _isScreenOff = MutableStateFlow(false)
    private val isScreenOff = _isScreenOff.asStateFlow()
    private val _isProximityCovered = MutableStateFlow(false)
    private val isProximityCovered = _isProximityCovered.asStateFlow()
    private var onlyWhenScreenOff = false
    private var activationDelaySeconds = 0
    private var orientationJob: Job? = null

    // DND Activation Schedule settings
    private var dndScheduleEnabled = false
    private var dndScheduleStartTime = "22:00"
    private var dndScheduleEndTime = "07:00"
    private var dndScheduleDays = setOf(1, 2, 3, 4, 5, 6, 7)

    // Helper vars
    private var flashlightDetectionEnabled = false
    private var mediaPlaybackDetectionEnabled = false
    private var headphoneDetectionEnabled = false
    private var proximityDetectionEnabled = false

    private lateinit var cameraManager: android.hardware.camera2.CameraManager


    private val screenStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    Log.d(TAG, "Screen turned OFF")
                    _isScreenOff.value = true
                    // Ensure wake lock is held when screen is off
                    acquireWakeLock()
                    
                    // Ensure sensors are monitoring when screen is off
                    if (!sensorService.isRegistered) {
                        Log.d(TAG, "Starting sensor monitoring because screen is OFF")
                        sensorService.startMonitoring()
                    }
                }
                Intent.ACTION_SCREEN_ON -> {
                    Log.d(TAG, "Screen turned ON")
                    _isScreenOff.value = false
                    // Release wake lock when screen is on to save battery
                    releaseWakeLock()
                    
                    // Optimization: if 'Only when screen off' is enabled, stop sensors when screen is on
                    if (onlyWhenScreenOff) {
                        Log.d(TAG, "Stopping sensor monitoring because screen is ON and 'onlyWhenScreenOff' is enabled")
                        sensorService.stopMonitoring()
                    }
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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: Starting FlipDetectorService")
        val notification = createServiceNotification()
        startForeground(SERVICE_NOTIFICATION_ID, notification)
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: Initializing FlipDetectorService")
        
        try {
            sensorService = SensorService(this, settingsRepository)
            dndService = DndService(this, settingsRepository, dndRepository)
            // settingsRepository is now injected by Hilt
            powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            cameraManager = getSystemService(Context.CAMERA_SERVICE) as android.hardware.camera2.CameraManager
            
            // Initialize screen state
            _isScreenOff.value = !powerManager.isInteractive
            Log.d(TAG, "Initial screen state: ${if (_isScreenOff.value) "OFF" else "ON"}")
            
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
            
            // Observe proximity setting
            serviceScope.launch {
                settingsRepository.getProximityDetectionEnabled().collect { enabled ->
                    proximityDetectionEnabled = enabled
                    Log.d(TAG, "Settings updated - Proximity detection: $enabled")
                    
                    val detectionManager = dev.robin.flip_2_dnd.core.ServiceLocator.getDetectionManager(this@FlipDetectorService)
                    if (enabled) {
                        detectionManager.registerProximityListener { covered ->
                            _isProximityCovered.value = covered
                        }
                    } else {
                        detectionManager.unregisterProximityListener()
                        _isProximityCovered.value = false
                    }
                }
            }
            
            // Observe settings changes
            serviceScope.launch {
                settingsRepository.getScreenOffOnlyEnabled().collect { enabled ->
                    onlyWhenScreenOff = enabled
                    Log.d(TAG, "Settings updated - Only when screen off: $enabled")
                    
                    if (enabled && !isScreenOff.value) {
                        Log.d(TAG, "Stopping sensors because screen is ON and 'onlyWhenScreenOff' was just enabled")
                        sensorService.stopMonitoring()
                    } else if (!enabled && !sensorService.isRegistered) {
                        Log.d(TAG, "Starting sensors because 'onlyWhenScreenOff' was just disabled")
                        sensorService.startMonitoring()
                    }
                }
            }

            serviceScope.launch {
                settingsRepository.getActivationDelay().collect { delay ->
                    activationDelaySeconds = delay
                    Log.d(TAG, "Settings updated - Activation delay: $delay seconds")
                }
            }
            
            // Observe detection settings
            serviceScope.launch {
                settingsRepository.getFlashlightDetectionEnabled().collect { enabled ->
                    flashlightDetectionEnabled = enabled
                    Log.d(TAG, "Settings updated - Flashlight detection: $enabled")
                }
            }

            serviceScope.launch {
                settingsRepository.getMediaPlaybackDetectionEnabled().collect { enabled ->
                    mediaPlaybackDetectionEnabled = enabled
                    Log.d(TAG, "Settings updated - Media playback detection: $enabled")
                }
            }

            serviceScope.launch {
                settingsRepository.getHeadphoneDetectionEnabled().collect { enabled ->
                    headphoneDetectionEnabled = enabled
                    Log.d(TAG, "Settings updated - Headphone detection: $enabled")
                }
            }


            // Observe DND activation schedule settings
            serviceScope.launch {
                settingsRepository.getDndScheduleEnabled().collect { enabled ->
                    dndScheduleEnabled = enabled
                    Log.d(TAG, "Settings updated - DND Schedule enabled: $enabled")
                }
            }

            serviceScope.launch {
                settingsRepository.getDndScheduleStartTime().collect { time ->
                    dndScheduleStartTime = time
                    Log.d(TAG, "Settings updated - DND Schedule start time: $time")
                }
            }

            serviceScope.launch {
                settingsRepository.getDndScheduleEndTime().collect { time ->
                    dndScheduleEndTime = time
                    Log.d(TAG, "Settings updated - DND Schedule end time: $time")
                }
            }

            serviceScope.launch {
                settingsRepository.getDndScheduleDays().collect { days ->
                    dndScheduleDays = days
                    Log.d(TAG, "Settings updated - DND Schedule days: $days")
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

    private fun handleOrientationChange(orientation: PhoneOrientation, screenOff: Boolean) {
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
                PhoneOrientation.FACE_DOWN -> {
                    // Check DND schedule if enabled
                    if (dndScheduleEnabled) {
                        val scheduleManager = dev.robin.flip_2_dnd.core.ServiceLocator.getScheduleManager(this@FlipDetectorService)
                        if (!scheduleManager.isWithinSchedule(dndScheduleStartTime, dndScheduleEndTime, dndScheduleDays)) {
                            Log.d(TAG, "Current time is outside DND schedule - Ignoring face down")
                            return
                        }
                    }

                    if (!currentDndState) {
                        // Only check screen off and stability when turning ON DND
                        if (onlyWhenScreenOff && !screenOff) {
                            Log.d(TAG, "Screen is ON and 'Only when screen off' is enabled - Ignoring face down")
                            return
                        }
                        
                         // Check Detection Settings
                        if (flashlightDetectionEnabled && dndService.isFlashlightOn) {
                            Log.d(TAG, "Flashlight is ON - Ignoring face down")
                            return
                        }

                        val detectionManager = dev.robin.flip_2_dnd.core.ServiceLocator.getDetectionManager(this@FlipDetectorService)

                        if (mediaPlaybackDetectionEnabled && detectionManager.isMediaPlaying()) {
                            Log.d(TAG, "Media is playing - Ignoring face down")
                            return
                        }

                        if (headphoneDetectionEnabled && detectionManager.areHeadphonesConnected()) {
                            Log.d(TAG, "Headphones are connected - Ignoring face down")
                            return
                        }

                        if (proximityDetectionEnabled && !detectionManager.isProximityCovered()) {
                            Log.d(TAG, "Proximity is NOT covered - Ignoring face down")
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
                            if (proximityDetectionEnabled && !_isProximityCovered.value) {
                                Log.d(TAG, "Proximity uncovered during delay - Cancelling DND toggle")
                                return@launch
                            }
                            if (sensorService.orientation.value == PhoneOrientation.FACE_DOWN) {
                                Log.d(TAG, "$activationDelaySeconds-second delay completed, phone still face down - Enabling DND")
                                dndService.toggleDnd()
                                showDndStateNotification(true)

                                // Handle Battery Saver
                                try {
                                    val enableBatterySaver = settingsRepository.getBatterySaverOnFlipEnabled().first()
                                    if (enableBatterySaver) {
                                        dev.robin.flip_2_dnd.core.ServiceLocator.getPowerController(this@FlipDetectorService)
                                            .setBatterySaverEnabled(true)
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
                                    dev.robin.flip_2_dnd.core.ServiceLocator.getPowerController(this@FlipDetectorService)
                                        .setBatterySaverEnabled(false)
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
            
             // Unregister torch callback and cleanup DndService
            if (::dndService.isInitialized) {
                dndService.cleanup()
            }
            
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

    private fun isWithinDndSchedule(): Boolean {
        return dev.robin.flip_2_dnd.core.ServiceLocator.getScheduleManager(this)
            .isWithinSchedule(dndScheduleStartTime, dndScheduleEndTime, dndScheduleDays)
    }

    private fun createNotificationChannels() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Service channel
        val serviceChannel = NotificationChannel(
            SERVICE_CHANNEL_ID,
            getString(R.string.notification_service_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.notification_service_channel_desc)
        }

        // Flip state channel
        val flipChannel = NotificationChannel(
            FLIP_CHANNEL_ID,
            getString(R.string.notification_flip_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.notification_flip_channel_desc)
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
                getString(R.string.seconds, activationDelaySeconds).let { getString(R.string.notification_dnd_activate_delay, it) }
            } else {
                getString(R.string.notification_dnd_activate_now)
            }
            val notification = NotificationCompat.Builder(this@FlipDetectorService, FLIP_CHANNEL_ID)
                .setContentTitle(getString(R.string.notification_flip_detected_title))
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
                .setContentTitle(getString(R.string.notification_dnd_state_changed_title))
                .setContentText(if (enabled) getString(R.string.notification_dnd_activated) else getString(R.string.notification_dnd_deactivated))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSilent(true) // Make notification silent
                .build()

            notificationManager.notify(DND_NOTIFICATION_ID, notification)
            Log.d(TAG, "DND state notification shown: DND ${if (enabled) "enabled" else "disabled"}")
        }
    }

    // Moved to PowerController

    private fun createServiceNotification(): Notification {
        val channelName = getString(R.string.notification_service_channel_name)
        val channel = NotificationChannel(
            SERVICE_CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.notification_service_channel_desc)
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
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.notification_monitoring_text))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .build()
    }
    // Moved to DetectionManager
}
