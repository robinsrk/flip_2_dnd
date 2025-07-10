package dev.robin.flip_2_dnd.presentation.onboarding

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.robin.flip_2_dnd.R
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

fun isNotificationPolicyAccessGranted(context: Context): Boolean {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    return notificationManager.isNotificationPolicyAccessGranted
}

fun isBatteryOptimizationDisabled(context: Context): Boolean {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    return powerManager.isIgnoringBatteryOptimizations(context.packageName)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val pagerState = rememberPagerState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context as Activity

    val dndPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (isNotificationPolicyAccessGranted(context)) {
            scope.launch {
                pagerState.animateScrollToPage(pagerState.currentPage + 1)
            }
        }
    }

    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = { 
                    Text(
                        text = "Flip 2 DND Setup",
                        style = MaterialTheme.typography.headlineLarge
                    )
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                actions = {
                    if (pagerState.currentPage > 0) {
                        Button(
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            },
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Previous",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                },
                floatingActionButton = {
                    Button(
                        onClick = {
                            when (pagerState.currentPage) {
                                5 -> onComplete()
                                else -> {
                                    scope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                    }
                                }
                            }
                        },
                        enabled = when (pagerState.currentPage) {
                            2 -> isNotificationPolicyAccessGranted(context)
                            3 -> isBatteryOptimizationDisabled(context)
                            else -> true
                        },
                        modifier = Modifier.padding(16.dp).also {
                            if (pagerState.currentPage == 3) {
                                println("Battery optimization disabled: ${isBatteryOptimizationDisabled(context)}")
                            }
                        }
                    ) {
                        Text(
                            text = when (pagerState.currentPage) {
                                5 -> "Get Started"
                                else -> "Next"
                            },
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                },
                tonalElevation = 0.dp
            )
        }
    ) { paddingValues ->
        val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            scope.launch {
                pagerState.animateScrollToPage(pagerState.currentPage + 1)
            }
        }
    }

    HorizontalPager(
            count = 6,
            state = pagerState,
            userScrollEnabled = false,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { page ->
            when (page) {
                0 -> WelcomePage(onNext = { scope.launch { pagerState.animateScrollToPage(page + 1) } })
                1 -> FeaturePage(onNext = { scope.launch { pagerState.animateScrollToPage(page + 1) } })
                2 -> DndPermissionPage(dndPermissionLauncher, onNext = { scope.launch { pagerState.animateScrollToPage(page + 1) } })
                3 -> BatteryOptimizationPage(onNext = { scope.launch { pagerState.animateScrollToPage(page + 1) } })
                4 -> NotificationPermissionPage(notificationPermissionLauncher, onNext = { scope.launch { pagerState.animateScrollToPage(page + 1) } })
                5 -> CompletePage(onNext = onComplete)
            }
        }
    }
}

@Composable
fun OnboardingCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            content()
        }
    }
}

@Composable
fun WelcomePage(onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OnboardingCard {
            Text(
                text = "Welcome to Flip 2 DND",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Control your phone's Do Not Disturb mode by simply flipping it face down.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun FeaturePage(onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OnboardingCard {
            Image(
                painter = painterResource(id = R.drawable.ic_dnd_on),
                contentDescription = "Feature illustration",
                modifier = Modifier.size(120.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Simple and Effective",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "To use this app effectively, we'll need a few permissions. Let's set those up together.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun DndPermissionPage(
    dndPermissionLauncher: ActivityResultLauncher<Intent>,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    var isPermissionGranted by remember { mutableStateOf(isNotificationPolicyAccessGranted(context)) }
    
    LaunchedEffect(Unit) {
        // Continuously check the permission state
        while (true) {
            isPermissionGranted = isNotificationPolicyAccessGranted(context)
            delay(500) // Check every 500ms
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OnboardingCard {
            Text(
                text = "DND Permission",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "We need permission to control Do Not Disturb mode.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    if (isPermissionGranted) {
                        onNext()
                    } else {
                        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                        dndPermissionLauncher.launch(intent)
                    }
                },
                enabled = !isPermissionGranted
            ) {
                Text(if (isPermissionGranted) "Permission Granted" else "Grant Permission")
            }
        }
    }
}

@Composable
fun BatteryOptimizationPage(onNext: () -> Unit) {
    val context = LocalContext.current
    var isOptimizationDisabled by remember { mutableStateOf(isBatteryOptimizationDisabled(context)) }
    
    val batteryOptimizationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (isBatteryOptimizationDisabled(context)) {
            onNext()
        }
    }
    
    LaunchedEffect(Unit) {
        // Continuously check the permission state
        while (true) {
            isOptimizationDisabled = isBatteryOptimizationDisabled(context)
            delay(500) // Check every 500ms
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OnboardingCard {
            Text(
                text = "Battery Optimization",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "To ensure reliable operation, we need to disable battery optimization for this app.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    if (isOptimizationDisabled) {
                        onNext()
                    } else {
                        val intent = Intent().apply {
                            action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                            data = Uri.parse("package:${context.packageName}")
                        }
                        batteryOptimizationLauncher.launch(intent)
                    }
                },
                enabled = !isOptimizationDisabled
            ) {
                Text(if (isOptimizationDisabled) "Permission Granted" else "Disable Battery Optimization")
            }
        }
    }
}

@Composable
fun NotificationPermissionPage(
    notificationPermissionLauncher: ActivityResultLauncher<String>,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OnboardingCard {
            Text(
                text = "Notifications",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "We need permission to send you notifications about the app's status.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            ) {
                Text("Grant Permission")
            }
        }
    }
}

@Composable
fun CompletePage(onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OnboardingCard {
            Text(
                text = "All Set!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "You're ready to use Flip 2 DND. Just flip your phone face down to enable Do Not Disturb mode.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}