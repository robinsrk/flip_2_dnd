package dev.robin.flip_2_dnd.presentation.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
	val useCustomVibration by viewModel.useCustomVibration.collectAsState()
	val customVibrationStrength by viewModel.customVibrationStrength.collectAsState()

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
            .padding(paddingValues)
            .verticalScroll(rememberScrollState()),
		) {
			Text(
				text = "Behavior",
				style = MaterialTheme.typography.titleLarge.copy(
					fontWeight = FontWeight.Bold
				),
				modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
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
				text = "Sound",
				style = MaterialTheme.typography.titleLarge.copy(
					fontWeight = FontWeight.Bold
				),
				modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
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
					val soundSheetState = rememberModalBottomSheetState()

					ListItem(
                        headlineContent = { Text("DND On Sound") },
                        supportingContent = { Text(dndOnSound.name) },
                        trailingContent = {
                            Icon(Icons.Default.ArrowDropDown, "Select sound")
                        },
                        modifier = Modifier.clickable { dndOnExpanded = true }
                    )

					if (dndOnExpanded) {
						ModalBottomSheet(
							onDismissRequest = { dndOnExpanded = false },
							sheetState = soundSheetState
						) {
							Column {
								viewModel.availableSounds.forEach { sound ->
									ListItem(
										headlineContent = { Text(sound.name) },
										trailingContent = {
											if (sound == dndOnSound) {
												Icon(
													imageVector = Icons.Default.Check,
													contentDescription = "Selected"
												)
											}
										},
										modifier = Modifier.clickable {
											viewModel.setDndOnSound(sound)
											dndOnExpanded = false
										}
									)
								}
							}
							Spacer(modifier = Modifier.height(20.dp))
						}
					}

					ListItem(
                        headlineContent = { Text("DND Off Sound") },
                        supportingContent = { Text(dndOffSound.name) },
                        trailingContent = {
                            Icon(Icons.Default.ArrowDropDown, "Select sound")
                        },
                        modifier = Modifier.clickable { dndOffExpanded = true }
                    )

					if (dndOffExpanded) {
						ModalBottomSheet(
							onDismissRequest = { dndOffExpanded = false },
							sheetState = soundSheetState
						) {
							Column {
								viewModel.availableSounds.forEach { sound ->
									ListItem(
										headlineContent = { Text(sound.name) },
										trailingContent = {
											if (sound == dndOffSound) {
												Icon(
													imageVector = Icons.Default.Check,
													contentDescription = "Selected"
												)
											}
										},
										modifier = Modifier.clickable {
											viewModel.setDndOffSound(sound)
											dndOffExpanded = false
										}
									)
								}
							}
							Spacer(modifier = Modifier.height(20.dp))
						}
					}

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
			}

			Divider(modifier = Modifier.padding(vertical = 16.dp))

			// Vibration Section
			Text(
				text = "Vibration",
				style = MaterialTheme.typography.titleLarge.copy(
					fontWeight = FontWeight.Bold
				),
				modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
			)

			Column {
				SettingsSwitchItem(
					title = "Vibration",
					description = "Vibrate when DND changes",
					checked = vibrationEnabled,
					onCheckedChange = { viewModel.setVibrationEnabled(it) },
				)

				if (vibrationEnabled) {
					var dndOnVibrationExpanded by remember { mutableStateOf(false) }
					var dndOffVibrationExpanded by remember { mutableStateOf(false) }
					val vibrationSheetState = rememberModalBottomSheetState()

					ListItem(
                        headlineContent = { Text("DND On Vibration") },
                        supportingContent = { Text(viewModel.dndOnVibration.collectAsState().value.displayName) },
                        trailingContent = {
                            Icon(Icons.Default.ArrowDropDown, "Select vibration pattern")
                        },
                        modifier = Modifier.clickable { dndOnVibrationExpanded = true }
                    )

					if (dndOnVibrationExpanded) {
						ModalBottomSheet(
							onDismissRequest = { dndOnVibrationExpanded = false },
							sheetState = vibrationSheetState
						) {
							Column {
								viewModel.availableVibrationPatterns.forEach { pattern ->
									ListItem(
										headlineContent = { Text(pattern.displayName) },
										trailingContent = {
											if (pattern == viewModel.dndOnVibration.collectAsState().value) {
												Icon(
													imageVector = Icons.Default.Check,
													contentDescription = "Selected"
												)
											}
										},
										modifier = Modifier.clickable {
											viewModel.setDndOnVibration(pattern)
											dndOnVibrationExpanded = false
										}
									)
								}
							}
							Spacer(modifier = Modifier.height(20.dp))
						}
					}

					ListItem(
                        headlineContent = { Text("DND Off Vibration") },
                        supportingContent = { Text(viewModel.dndOffVibration.collectAsState().value.displayName) },
                        trailingContent = {
                            Icon(Icons.Default.ArrowDropDown, "Select vibration pattern")
                        },
                        modifier = Modifier.clickable { dndOffVibrationExpanded = true }
                    )

					if (dndOffVibrationExpanded) {
						ModalBottomSheet(
							onDismissRequest = { dndOffVibrationExpanded = false },
							sheetState = vibrationSheetState
						) {
							Column {
								viewModel.availableVibrationPatterns.forEach { pattern ->
									ListItem(
										headlineContent = { Text(pattern.displayName) },
										trailingContent = {
											if (pattern == viewModel.dndOffVibration.collectAsState().value) {
												Icon(
													imageVector = Icons.Default.Check,
													contentDescription = "Selected"
												)
											}
										},
										modifier = Modifier.clickable {
											viewModel.setDndOffVibration(pattern)
											dndOffVibrationExpanded = false
										}
									)
								}
							}
							Spacer(modifier = Modifier.height(20.dp))
						}
					}

					SettingsSwitchItem(
						title = "Custom Vibration",
						description = "Use custom vibration strength instead of system default",
						checked = useCustomVibration,
						onCheckedChange = { viewModel.setUseCustomVibration(it) },
					)

					if (useCustomVibration) {
						ListItem(
							headlineContent = { Text("Vibration Strength") },
							trailingContent = {
								var sliderPosition by remember { mutableStateOf(customVibrationStrength) }
								LaunchedEffect(customVibrationStrength) {
									sliderPosition = customVibrationStrength
								}
								Slider(
									value = sliderPosition,
									onValueChange = { newStrength ->
										sliderPosition = newStrength
									},
									onValueChangeFinished = {
										viewModel.setCustomVibrationStrength(sliderPosition)
									},
									modifier = Modifier.width(200.dp)
								)
							}
						)
					}
				}
			}
            Divider(modifier = Modifier.padding(vertical = 16.dp))

            Text(
                text = "Extras",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
            )

            Column {
                ListItem(
                    headlineContent = { 
                        Text(
                            "Join Telegram",
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    supportingContent = { Text("Join our Telegram community") },
                    leadingContent = {
                        Icon(
                            painter = painterResource(id = R.drawable.telegram),
                            contentDescription = "Telegram Icon",
														tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.width(24.dp)
                        )
                    },
                    modifier = Modifier.clickable {
                        val telegramUsername = "flip_2_dnd"
                        val telegramUri = "tg://resolve?domain=$telegramUsername"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(telegramUri))
                        try {
                            context.startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(context, "Telegram app not found", Toast.LENGTH_SHORT).show()
                            println("No activity found to handle the intent: $e")
                        }
                    }
                )

                ListItem(
                    headlineContent = { 
                        Text(
                            "Support Developer",
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    supportingContent = { Text("Help keep this app free and improving") },
                    leadingContent = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_coin),
                            contentDescription = "Support Icon",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier.clickable(onClick = onDonateClick)
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
