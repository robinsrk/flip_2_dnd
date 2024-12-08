package dev.robin.flip_2_dnd

import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.robin.flip_2_dnd.presentation.main.MainScreen
import dev.robin.flip_2_dnd.presentation.main.MainViewModel
import dev.robin.flip_2_dnd.presentation.settings.SettingsScreen
import dev.robin.flip_2_dnd.services.FlipDetectorService
import dev.robin.flip_2_dnd.ui.theme.Flip_2_DNDTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    
    private val dndPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        checkAndStartService()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        checkAndStartService()

        setContent {
            Flip_2_DNDTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "main"
                ) {
                    composable("main") {
                        val state by mainViewModel.state.collectAsState()
                        MainScreen(
                            state = state,
                            onSettingsClick = {
                                navController.navigate("settings")
                            }
                        )
                    }
                    composable("settings") {
                        SettingsScreen(
                            navController = navController
                        )
                    }
                }
            }
        }
    }

    private fun checkAndStartService() {
        val notificationPolicyGranted = isNotificationPolicyAccessGranted()
        val batteryOptimizationDisabled = isBatteryOptimizationDisabled()

        // Always start the service
        startFlipDetectorService()

        // If permissions are not granted, show a warning
        if (!notificationPolicyGranted || !batteryOptimizationDisabled) {
            // Optional: Add a toast or dialog to inform user about missing permissions
            Toast.makeText(
                this, 
                "Please grant all permissions for full functionality", 
                Toast.LENGTH_LONG
            ).show()

            if (!notificationPolicyGranted) {
                requestNotificationPolicyAccess()
            }

            if (!batteryOptimizationDisabled) {
                requestDisableBatteryOptimization()
            }
        }
    }

    private fun isNotificationPolicyAccessGranted(): Boolean {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.isNotificationPolicyAccessGranted
    }

    private fun requestNotificationPolicyAccess() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
        dndPermissionLauncher.launch(intent)
    }

    private fun isBatteryOptimizationDisabled(): Boolean {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(packageName)
    }

    private fun requestDisableBatteryOptimization() {
        val intent = Intent().apply {
            action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            data = Uri.parse("package:$packageName")
        }
        startActivity(intent)
    }

    private fun startFlipDetectorService() {
        Intent(this, FlipDetectorService::class.java).also { intent ->
            startForegroundService(intent)
        }
    }
}