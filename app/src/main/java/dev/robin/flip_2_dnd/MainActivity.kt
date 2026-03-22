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
import kotlinx.coroutines.launch
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
import androidx.compose.ui.res.stringResource
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.foundation.clickable
import android.content.ClipData
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.text.font.FontVariation
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.robin.flip_2_dnd.core.SettingsRepository

import dev.robin.flip_2_dnd.presentation.navigation.AppNavigation
import dev.robin.flip_2_dnd.presentation.onboarding.OnboardingScreen
import dev.robin.flip_2_dnd.services.FlipDetectorService
import dev.robin.flip_2_dnd.services.TurnScreenOffService
import dev.robin.flip_2_dnd.ui.theme.Flip_2_DNDTheme
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  private val mainViewModel: MainViewModel by viewModels()
  private var showOnboarding = true
  private val PREFS_NAME = "FlipDndPrefs"
  private val ONBOARDING_COMPLETED = "onboarding_completed"
  private val LAST_SEEN_VERSION = "last_seen_version"

  private var isPermissionMissing by mutableStateOf(false)
  private val missingPermissions = mutableStateListOf<String>()

  @Inject
  lateinit var settingsRepository: SettingsRepository

  private var turnScreenOff = false

  private val dndPermissionLauncher =
    registerForActivityResult(
      ActivityResultContracts.StartActivityForResult(),
    ) { checkAndStartService() }

  private val accessibilityPermissionLauncher =
    registerForActivityResult(
      ActivityResultContracts.StartActivityForResult(),
    ) {}
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

    // Check for updates (Pro version only)
    dev.robin.flip_2_dnd.core.ServiceLocator.getFeatureManager(this).checkForUpdate(false)

    lifecycleScope.launch {
      turnScreenOff = settingsRepository.getTurnScreenOffEnabled().first()
    }

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
      var showRamadanPopup by remember { mutableStateOf(false) }

      LaunchedEffect(Unit) {
        if (!dev.robin.flip_2_dnd.core.ServiceLocator.getFeatureManager(this@MainActivity).isPro() && !showOnboarding) {
          showRamadanPopup = true
        }
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

              if (!dev.robin.flip_2_dnd.core.ServiceLocator.getFeatureManager(this@MainActivity).isPro()) {
                showRamadanPopup = true
              }

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

          if (showRamadanPopup) {
            RamadanPopup(
              onDismiss = { showRamadanPopup = false }
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
                "Accessibility" -> requestAccessibilityPermission()
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
      ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.POST_NOTIFICATIONS
      ) == PackageManager.PERMISSION_GRANTED
    } else {
      true
    }

    // Update missing permissions state
    missingPermissions.clear()
    if (!notificationPolicyGranted) missingPermissions.add("DND")
    if (!batteryOptimizationDisabled) missingPermissions.add("Battery")
    if (
      TurnScreenOffService.isTurnScreenOffSupported() &&
      turnScreenOff && !isAccessibilityPermissionGranted()) missingPermissions.add("Accessibility")

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

  private fun isAccessibilityPermissionGranted() : Boolean {
    return TurnScreenOffService.isAccessibilityPermissionGranted(this)
  }

  private fun requestAccessibilityPermission() {
    val intent = TurnScreenOffService.getRequestAccessibilityPermissionIntent()
    accessibilityPermissionLauncher.launch(intent)
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

  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  private fun RamadanPopup(onDismiss: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val clipboard = LocalClipboard.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val patreonUrl = "https://www.patreon.com/posts/flip-2-dnd-150924870"

    ModalBottomSheet(
      onDismissRequest = onDismiss,
      containerColor = MaterialTheme.colorScheme.surface,
      tonalElevation = 8.dp,
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp)
          .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Icon(
          imageVector = Icons.Default.Star,
          contentDescription = null,
          modifier = Modifier
            .size(48.dp)
            .padding(bottom = 16.dp),
          tint = MaterialTheme.colorScheme.primary
        )
        Text(
          text = stringResource(id = R.string.ramadan_kareem),
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold,
          textAlign = TextAlign.Center,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
          text = stringResource(id = R.string.ramadan_message),
          style = MaterialTheme.typography.bodyLarge,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(bottom = 24.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Button(
            onClick = {
              val intent = Intent(Intent.ACTION_VIEW, Uri.parse(patreonUrl))
              context.startActivity(intent)
              onDismiss()
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
          ) {
            Text(stringResource(id = R.string.get_pro), style = MaterialTheme.typography.titleMedium)
          }
          TextButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth().height(56.dp)
          ) {
            Text(stringResource(id = R.string.maybe_later), style = MaterialTheme.typography.titleMedium)
          }
        }
      }
    }
  }
}
