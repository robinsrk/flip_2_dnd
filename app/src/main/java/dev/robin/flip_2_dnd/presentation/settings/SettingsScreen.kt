package dev.robin.flip_2_dnd.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import dev.robin.flip_2_dnd.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    navController: NavController? = null
) {
    val soundEnabled by viewModel.soundEnabled.collectAsState()
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsState()
    val screenOffOnly by viewModel.screenOffOnly.collectAsState()
    val priorityDndEnabled by viewModel.priorityDndEnabled.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    if (navController != null) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_arrow_back),
                                contentDescription = "Back"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Behavior",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Column {
                SettingsSwitchItem(
                    title = "Screen Off Only",
                    description = "Enable DND only when screen is off",
                    checked = screenOffOnly,
                    onCheckedChange = { viewModel.setScreenOffOnly(it) }
                )
                
                SettingsSwitchItem(
                    title = "Priority DND",
                    description = "Enable Priority mode DND instead of Total Silence",
                    checked = priorityDndEnabled,
                    onCheckedChange = { viewModel.setPriorityDndEnabled(it) }
                )
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            Text(
                text = "Notifications",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Column {
                SettingsSwitchItem(
                    title = "Sound",
                    description = "Play sound when DND changes",
                    checked = soundEnabled,
                    onCheckedChange = { viewModel.setSoundEnabled(it) }
                )

                SettingsSwitchItem(
                    title = "Vibration",
                    description = "Vibrate when DND changes",
                    checked = vibrationEnabled,
                    onCheckedChange = { viewModel.setVibrationEnabled(it) }
                )
            }
        }
    }
}

@Composable
fun SettingsSwitchItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { 
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            ) 
        },
        supportingContent = { 
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ) 
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    )
}
