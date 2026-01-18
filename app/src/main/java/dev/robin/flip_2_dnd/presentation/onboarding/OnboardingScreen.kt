package dev.robin.flip_2_dnd.presentation.onboarding

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import dev.robin.flip_2_dnd.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    // Hoist state here to ensure button updates
    var dndGranted by remember { mutableStateOf(isNotificationPolicyAccessGranted(context)) }
    var batteryGranted by remember { mutableStateOf(isBatteryOptimizationDisabled(context)) }

    // Poll for updates (e.g. when user comes back from settings)
    LaunchedEffect(Unit) {
        while(true) {
            dndGranted = isNotificationPolicyAccessGranted(context)
            batteryGranted = isBatteryOptimizationDisabled(context)
            delay(500)
        }
    }

    val dndPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) {
        // State updates via polling
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // State updates via polling
    }

    // Background color surface for the whole screen
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding() 
        ) {
            // Top section: Empty for now (Skip button removed)
            Spacer(modifier = Modifier.height(32.dp))

            // Middle section: Pager
            HorizontalPager(
                count = 4,
                state = pagerState,
                userScrollEnabled = false,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                when (page) {
                    0 -> OnboardingContentPage(
                        title = "Welcome to Flip 2 DND",
                        description = "Control your phone's Do Not Disturb mode by simply flipping it face down.",
                        iconRes = R.drawable.ic_launcher,
                        iconTint = Color.Unspecified
                    )
                    1 -> OnboardingContentPage(
                        title = "Simple & Effective",
                        description = "Focus on what matters. Flip your phone to silence interruptions instantly.",
                        iconRes = R.drawable.ic_dnd_on,
                        iconTint = MaterialTheme.colorScheme.secondary
                    )
                    2 -> UnifiedPermissionsPage(
                        dndPermissionLauncher = dndPermissionLauncher,
                        notificationPermissionLauncher = notificationPermissionLauncher,
                        dndGranted = dndGranted,
                        batteryGranted = batteryGranted
                    )
                    3 -> OnboardingContentPage(
                        title = "You're All Set!",
                        description = "Flip your phone face down to try it out.",
                        iconVector = Icons.Default.Check,
                        iconTint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Bottom section: Indicators and Navigation
            BottomNavigationSection(
                pagerState = pagerState,
                onNext = {
                    scope.launch {
                        if (pagerState.currentPage < 3) {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        } else {
                            onComplete()
                        }
                    }
                },
                onComplete = onComplete,
                dndGranted = dndGranted,
                batteryGranted = batteryGranted
            )
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun BottomNavigationSection(
    pagerState: PagerState,
    onNext: () -> Unit,
    onComplete: () -> Unit,
    dndGranted: Boolean,
    batteryGranted: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Page Indicators
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(4) { index ->
                val isSelected = pagerState.currentPage == index
                val width = if (isSelected) 24.dp else 8.dp
                val color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant

                Box(
                    modifier = Modifier
                        .height(8.dp)
                        .width(width)
                        .clip(CircleShape)
                        .background(color)
                        .animateContentSize(animationSpec = spring<IntSize>(stiffness = Spring.StiffnessLow))
                )
            }
        }

        // Action Button
        val isLastPage = pagerState.currentPage == 3
        val canProceed = when(pagerState.currentPage) {
            2 -> dndGranted && batteryGranted
            else -> true
        }

        Button(
            onClick = {
                if (isLastPage) onComplete() else onNext()
            },
            enabled = if (isLastPage) true else canProceed,
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
            modifier = Modifier.height(50.dp)
        ) {
            Text(
                text = if (isLastPage) "Get Started" else "Next",
                style = MaterialTheme.typography.labelLarge
            )
            if (!isLastPage) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.NavigateNext,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
fun OnboardingContentPage(
    title: String,
    description: String,
    iconRes: Int? = null,
    iconVector: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            if (iconVector != null) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = iconTint
                )
            } else if (iconRes != null) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = iconTint
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun UnifiedPermissionsPage(
    dndPermissionLauncher: ActivityResultLauncher<Intent>,
    notificationPermissionLauncher: ActivityResultLauncher<String>,
    dndGranted: Boolean,
    batteryGranted: Boolean
) {
    val context = LocalContext.current
    
    // We rely on parent passed state now
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Setup Permissions",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
             text = "Grant the following permissions to ensure Flip 2 DND works correctly.",
             style = MaterialTheme.typography.bodyMedium,
             textAlign = TextAlign.Center,
             color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Permission List
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            
            PermissionItem(
                title = "Do Not Disturb Access",
                description = "Required to toggle DND mode.",
                icon = Icons.Default.Settings,
                isGranted = dndGranted,
                isRequired = true,
                onClick = {
                    if (!dndGranted) {
                        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                        dndPermissionLauncher.launch(intent)
                    }
                }
            )

            PermissionItem(
                title = "Battery Optimization",
                description = "Required for background reliability.",
                icon = Icons.Default.BatteryAlert,
                isGranted = batteryGranted,
                isRequired = true,
                onClick = {
                    if (!batteryGranted) {
                        val intent = Intent().apply {
                            action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                            data = Uri.parse("package:${context.packageName}")
                        }
                        context.startActivity(intent)
                    }
                }
            )

            PermissionItem(
                title = "Notifications",
                description = "Optional. Shows service status.",
                icon = Icons.Default.Notifications,
                isGranted = false, // Polling this is complex without permission helper, keep simple
                isRequired = false,
                onClick = {
                    notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            )
        }
    }
}

@Composable
fun PermissionItem(
    title: String,
    description: String,
    icon: ImageVector,
    isGranted: Boolean,
    isRequired: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isGranted) 
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) 
    else 
        MaterialTheme.colorScheme.surface

    val borderColor = if (isGranted) Color.Transparent else MaterialTheme.colorScheme.outlineVariant

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = !isGranted, onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = if (!isGranted) androidx.compose.foundation.BorderStroke(1.dp, borderColor) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (isRequired) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "*",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            if (isGranted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Granted",
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Button(
                    onClick = onClick,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Grant", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}