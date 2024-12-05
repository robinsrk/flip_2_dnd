package dev.robin.flip_2_dnd

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
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.robin.flip_2_dnd.presentation.main.MainScreen
import dev.robin.flip_2_dnd.presentation.main.MainViewModel
import dev.robin.flip_2_dnd.presentation.settings.SettingsScreen
import dev.robin.flip_2_dnd.presentation.settings.SettingsViewModel
import dev.robin.flip_2_dnd.services.FlipDetectorService
import dev.robin.flip_2_dnd.ui.theme.Flip_2_DNDTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        checkPermissions()
        startFlipDetectorService()

        setContent {
            Flip_2_DNDTheme {
                val navController = rememberNavController()
                val mainState by mainViewModel.state.collectAsState()

                NavHost(
                    navController = navController,
                    startDestination = "main"
                ) {
                    composable("main") {
                        MainScreen(
                            state = mainState,
                            onSettingsClick = { navController.navigate("settings") }
                        )
                    }
                    composable("settings") {
                        SettingsScreen(
                            state = mainState,
                            onBackClick = { navController.popBackStack() },
                            onScreenOffOnlyChange = settingsViewModel::setScreenOffOnly,
                            onVibrationChange = settingsViewModel::setVibrationEnabled,
                            onSoundChange = settingsViewModel::setSoundEnabled
                        )
                    }
                }
            }
        }
    }

    private fun startFlipDetectorService() {
        Intent(this, FlipDetectorService::class.java).also { intent ->
            startForegroundService(intent)
        }
    }

    private fun checkPermissions() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!notificationManager.isNotificationPolicyAccessGranted) {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
        }

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
            startActivity(Intent().apply {
                action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                data = Uri.parse("package:$packageName")
            })
        }
    }
}