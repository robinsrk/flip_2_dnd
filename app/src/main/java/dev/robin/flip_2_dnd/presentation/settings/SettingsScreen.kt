package dev.robin.flip_2_dnd.presentation.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.foundation.clickable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue as getValueBy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import dev.robin.flip_2_dnd.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    navController: NavController? = null,
    onDonateClick: () -> Unit,
) {
    val context = LocalContext.current
    val soundEnabled by viewModel.soundEnabled.collectAsState()
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsState()
    val screenOffOnly by viewModel.screenOffOnly.collectAsState()
    val priorityDndEnabled by viewModel.priorityDndEnabled.collectAsState()
    val dndOnSound by viewModel.dndOnSound.collectAsState()
    val dndOffSound by viewModel.dndOffSound.collectAsState()
    val useCustomVolume by viewModel.useCustomVolume.collectAsState()
    val customVolume by viewModel.customVolume.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    ) 
                }
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
        ) {
            Text(
                text = "Behavior",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 8.dp),
            )

            Column {
                SettingsSwitchItem(
                    title = "Screen Off Only",
                    description = "Enable DND only when screen is off",
                    checked = screenOffOnly,
                    onCheckedChange = { viewModel.setScreenOffOnly(it) },
                )

                SettingsSwitchItem(
                    title = "Priority DND",
                    description = "Enable Priority mode DND instead of Total Silence",
                    checked = priorityDndEnabled,
                    onCheckedChange = { viewModel.setPriorityDndEnabled(it) },
                )
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            Text(
                text = "Notifications",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 8.dp),
            )

            Column {
                SettingsSwitchItem(
                    title = "Sound",
                    description = "Play sound when DND changes",
                    checked = soundEnabled,
                    onCheckedChange = { viewModel.setSoundEnabled(it) },
                )

                if (soundEnabled) {
                    var dndOnExpanded by remember { mutableStateOf(false) }
                    var dndOffExpanded by remember { mutableStateOf(false) }

                    ListItem(
                        headlineContent = { Text("DND On Sound") },
                        supportingContent = { Text(dndOnSound.name) },
                        trailingContent = {
                            IconButton(onClick = { dndOnExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, "Select sound")
                            }
                            DropdownMenu(
                                expanded = dndOnExpanded,
                                onDismissRequest = { dndOnExpanded = false }
                            ) {
                                viewModel.availableSounds.forEach { sound ->
                                    DropdownMenuItem(
                                        text = { Text(text = sound.name) },
                                        onClick = {
                                            viewModel.setDndOnSound(sound)
                                            dndOnExpanded = false
                                        },
                                        modifier = Modifier.clickable { 
                                            viewModel.setDndOnSound(sound)
                                            dndOnExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    )

                    ListItem(
                        headlineContent = { Text("DND Off Sound") },
                        supportingContent = { Text(dndOffSound.name) },
                        trailingContent = {
                            IconButton(onClick = { dndOffExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, "Select sound")
                            }
                            DropdownMenu(
                                expanded = dndOffExpanded,
                                onDismissRequest = { dndOffExpanded = false }
                            ) {
                                viewModel.availableSounds.forEach { sound ->
                                    DropdownMenuItem(
                                        text = { Text(text = sound.name) },
                                        onClick = {
                                            viewModel.setDndOffSound(sound)
                                            dndOffExpanded = false
                                        },
                                        modifier = Modifier.clickable { 
                                            viewModel.setDndOffSound(sound)
                                            dndOffExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    )

                    SettingsSwitchItem(
                        title = "Custom Volume",
                        description = "Use custom volume instead of system media volume",
                        checked = useCustomVolume,
                        onCheckedChange = { viewModel.setUseCustomVolume(it) },
                    )

                    if (useCustomVolume) {
                        ListItem(
                            headlineContent = { Text("Sound Volume") },
                            trailingContent = {
                                var sliderPosition by remember { mutableStateOf(customVolume) }
                                LaunchedEffect(customVolume) {
                                    sliderPosition = customVolume
                                }
                                Slider(
                                    value = sliderPosition,
                                    onValueChange = { newVolume ->
                                        sliderPosition = newVolume
                                    },
                                    onValueChangeFinished = {
                                        viewModel.setCustomVolume(sliderPosition)
                                    },
                                    modifier = Modifier.width(200.dp)
                                )
                            }
                        )
                    }
                }

                SettingsSwitchItem(
                    title = "Vibration",
                    description = "Vibrate when DND changes",
                    checked = vibrationEnabled,
                    onCheckedChange = { viewModel.setVibrationEnabled(it) },
                )
            }
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                Button(onClick = {
                    val telegramUsername = "flip_2_dnd"
                    val telegramUri = "tg://resolve?domain=$telegramUsername"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(telegramUri))
                    try {
                        context.startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        // Handle the case where no activity can handle the intent
                        // For example, show a toast or log the error
                        Toast.makeText(context, "Telegram app not found", Toast.LENGTH_SHORT).show()
                        println("No activity found to handle the intent: $e")
                    }
                }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.telegram),
                            contentDescription = "Logo",
                            modifier = Modifier.width(30.dp),
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("Join telegram")
                    }
                }
                Spacer(Modifier.width(10.dp))
                Button(onClick = onDonateClick) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.donation),
                            contentDescription = "Logo",
                            modifier = Modifier.width(30.dp),
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("Support")
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSwitchItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
        },
        supportingContent = {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        },
    )
}
