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
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.robin.flip_2_dnd.R
import dev.robin.flip_2_dnd.presentation.donation.DonationScreen
import dev.robin.flip_2_dnd.presentation.main.MainScreen
import dev.robin.flip_2_dnd.presentation.main.MainViewModel
import dev.robin.flip_2_dnd.presentation.settings.SettingsScreen
import dev.robin.flip_2_dnd.presentation.navigation.AppNavigation
import dev.robin.flip_2_dnd.presentation.onboarding.OnboardingScreen
import dev.robin.flip_2_dnd.services.FlipDetectorService
import dev.robin.flip_2_dnd.ui.theme.Flip_2_DNDTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  private val mainViewModel: MainViewModel by viewModels()
  private var showOnboarding = true
  private val PREFS_NAME = "FlipDndPrefs"
  private val ONBOARDING_COMPLETED = "onboarding_completed"

  private val dndPermissionLauncher =
          registerForActivityResult(
                  ActivityResultContracts.StartActivityForResult(),
          ) { checkAndStartService() }

  @OptIn(ExperimentalMaterial3Api::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Load onboarding state from SharedPreferences
    val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
    showOnboarding = !prefs.getBoolean(ONBOARDING_COMPLETED, false)

    setContent {
      var showOnboardingState by remember { mutableStateOf(showOnboarding) }
      Flip_2_DNDTheme {
        if (showOnboardingState) {
          OnboardingScreen(
            onComplete = {
              showOnboardingState = false
              showOnboarding = false
              // Save onboarding completion state
              prefs.edit().putBoolean(ONBOARDING_COMPLETED, true).apply()
              checkAndStartService()
            }
          )
        } else {
          AppNavigation()
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
                      Toast.LENGTH_LONG,
              )
              .show()

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
    val intent =
            Intent().apply {
              action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
              data = Uri.parse("package:$packageName")
            }
    startActivity(intent)
  }

  private fun startFlipDetectorService() {
    Intent(this, FlipDetectorService::class.java).also { intent -> startForegroundService(intent) }
  }
}
