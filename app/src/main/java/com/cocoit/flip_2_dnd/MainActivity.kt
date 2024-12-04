package com.cocoit.flip_2_dnd

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cocoit.flip_2_dnd.services.FlipDetectorService
import com.cocoit.flip_2_dnd.services.DndService
import com.cocoit.flip_2_dnd.services.SensorService
import com.cocoit.flip_2_dnd.settings.SettingsManager
import com.cocoit.flip_2_dnd.ui.SettingsScreen
import com.cocoit.flip_2_dnd.ui.theme.Flip_2_DNDTheme

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    private lateinit var dndService: DndService
    private lateinit var settingsManager: SettingsManager
    private var sensorService: SensorService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Log.d(TAG, "onCreate: Starting MainActivity")

        // Request battery optimization exemption
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent().apply {
                    action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting battery optimization exemption: ${e.message}", e)
        }

        // Check for DND permission
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!notificationManager.isNotificationPolicyAccessGranted) {
            Log.d(TAG, "DND permission not granted, requesting...")
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            startActivity(intent)
            return
        }

        try {
            dndService = DndService(this)
            sensorService = SensorService(this)
            settingsManager = SettingsManager(this)
            
            // Start the foreground service
            Log.d(TAG, "Starting FlipDetectorService")
            val serviceIntent = Intent(this, FlipDetectorService::class.java)
            startForegroundService(serviceIntent)

            setContent {
                Flip_2_DNDTheme {
                    var showSettings by remember { mutableStateOf(false) }

                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        if (showSettings) {
                            SettingsScreen(
                                settingsManager = settingsManager,
                                onBackClick = { showSettings = false }
                            )
                        } else {
                            MainScreen(
                                sensorService = sensorService!!,
                                dndService = dndService,
                                onSettingsClick = { showSettings = true }
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing services: ${e.message}", e)
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            dndService.updateDndStatus()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onResume: ${e.message}", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        // Don't stop the service here as it should run in the background
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainScreen(
    sensorService: SensorService,
    dndService: DndService,
    onSettingsClick: () -> Unit
) {
    val orientation by remember { sensorService.orientation }.collectAsState()
    val isDndEnabled by dndService.isDndEnabled.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Settings button in top-right
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Phone orientation animation
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "Phone orientation",
                    modifier = Modifier
                        .size(120.dp)
                        .rotate(
                            when (orientation) {
                                "Face down" -> 180f
                                else -> 0f
                            }
                        ),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Status text with animation
            Text(
                text = "Phone is",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            AnimatedContent(
                targetState = orientation,
                transitionSpec = {
                    fadeIn() + slideInVertically() with 
                    fadeOut() + slideOutVertically()
                }
            ) { targetOrientation ->
                Text(
                    text = targetOrientation,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // DND status card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDndEnabled)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Do Not Disturb",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isDndEnabled)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isDndEnabled) "Active" else "Inactive",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isDndEnabled)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = if (isDndEnabled)
                            Icons.Outlined.Notifications
                        else
                            Icons.Outlined.Notifications,
                        contentDescription = "DND Status",
                        tint = if (isDndEnabled)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}