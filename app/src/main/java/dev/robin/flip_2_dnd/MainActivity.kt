package dev.robin.flip_2_dnd

import android.Manifest
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.robin.flip_2_dnd.R
import dev.robin.flip_2_dnd.presentation.donation.DonationScreen
import dev.robin.flip_2_dnd.presentation.main.ChangelogBottomSheet
import dev.robin.flip_2_dnd.presentation.main.MainScreen
import dev.robin.flip_2_dnd.presentation.main.MainViewModel

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
  private val LAST_SEEN_VERSION = "last_seen_version"
  
  private var isPermissionMissing by mutableStateOf(false)
  private val missingPermissions = mutableStateListOf<String>()

  private val dndPermissionLauncher =
          registerForActivityResult(
                  ActivityResultContracts.StartActivityForResult(),
          ) { checkAndStartService() }

  private val notificationPermissionLauncher =
          registerForActivityResult(
                  ActivityResultContracts.RequestPermission(),
          ) { isGranted ->
              if (isGranted) {
                  checkAndStartService()
              } else {
                  Toast.makeText(
                          this,
                          getString(R.string.error_notification_permission_required),
                          Toast.LENGTH_LONG
                  ).show()
              }
          }

  @OptIn(ExperimentalMaterial3Api::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Load onboarding state from SharedPreferences
    val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
    showOnboarding = !prefs.getBoolean(ONBOARDING_COMPLETED, false)

    val currentVersionCode = try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        packageManager.getPackageInfo(packageName, 0).longVersionCode
      } else {
        @Suppress("DEPRECATION")
        packageManager.getPackageInfo(packageName, 0).versionCode.toLong()
      }
    } catch (e: Exception) {
      0L
    }

    val lastSeenVersion = prefs.getLong(LAST_SEEN_VERSION, 0L)

    setContent {
      var showOnboardingState by remember { mutableStateOf(showOnboarding) }
      var showChangelog by remember { 
        mutableStateOf(!showOnboarding && currentVersionCode > lastSeenVersion) 
      }

      Flip_2_DNDTheme {
        val isDarkTheme = isSystemInDarkTheme()
        val surfaceColor = MaterialTheme.colorScheme.surface.toArgb()
        
        remember(isDarkTheme) {
          enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
              android.graphics.Color.TRANSPARENT,
              android.graphics.Color.TRANSPARENT,
            ) { isDarkTheme },
            navigationBarStyle = SystemBarStyle.auto(
              surfaceColor,
              surfaceColor,
            ) { isDarkTheme }
          )
          null
        }

        if (showOnboardingState) {
          OnboardingScreen(
            onComplete = {
              showOnboardingState = false
              showOnboarding = false
              // Save onboarding completion state
              prefs.edit().putBoolean(ONBOARDING_COMPLETED, true).apply()
              
              // Also update last seen version when onboarding is completed
              // so changelog doesn't show immediately after onboarding
              prefs.edit().putLong(LAST_SEEN_VERSION, currentVersionCode).apply()
              
              checkAndStartService()
            }
          )
        } else {
          AppNavigation()
          
          if (showChangelog) {
            ChangelogBottomSheet(
              onDismiss = {
                showChangelog = false
                prefs.edit().putLong(LAST_SEEN_VERSION, currentVersionCode).apply()
              }
            )
          }
        }

        if (!showOnboardingState && isPermissionMissing) {
          PermissionDialog(
            missingPermissions = missingPermissions,
            onGrantClick = { permission ->
              when (permission) {
                "DND" -> requestNotificationPolicyAccess()
                "Battery" -> requestDisableBatteryOptimization()
                "Notification" -> {
                  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                  }
                }
              }
            }
          )
        }
      }
    }
    
    // Check permissions every time the app opens
    if (!showOnboarding) {
        checkAndStartService()
    }
  }

  @Composable
  private fun PermissionDialog(
    missingPermissions: List<String>,
    onGrantClick: (String) -> Unit
  ) {
    AlertDialog(
      onDismissRequest = { /* Cannot dismiss mandatory dialog */ },
      title = { Text(getString(R.string.permission_required_title)) },
      text = {
        Column {
          Text(getString(R.string.permission_required_desc))
          Spacer(modifier = Modifier.height(16.dp))
          missingPermissions.forEach { permission ->
            Row(
              modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = when (permission) {
                  "DND" -> getString(R.string.permission_dnd)
                  "Battery" -> getString(R.string.permission_battery)
                  "Notification" -> getString(R.string.permission_notification)
                  else -> permission
                },
                modifier = Modifier.weight(1f)
              )
              Button(onClick = { onGrantClick(permission) }) {
                Text(getString(R.string.grant))
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { checkAndStartService() }) {
          Text(getString(R.string.check_again))
        }
      }
    )
  }

  private fun checkAndStartService() {
    val notificationPolicyGranted = isNotificationPolicyAccessGranted()
    val batteryOptimizationDisabled = isBatteryOptimizationDisabled()
    val notificationPermissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }

    // Update missing permissions state
    missingPermissions.clear()
    if (!notificationPolicyGranted) missingPermissions.add("DND")
    if (!batteryOptimizationDisabled) missingPermissions.add("Battery")

    isPermissionMissing = missingPermissions.isNotEmpty()

    // Start service if mandatory permissions are granted
    if (notificationPolicyGranted && batteryOptimizationDisabled) {
        startFlipDetectorService()
    }

    // If mandatory permissions are missing, the dialog will be shown via Compose state
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
