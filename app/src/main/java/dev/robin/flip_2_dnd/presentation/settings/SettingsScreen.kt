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

				val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
				SettingsSwitchItem(
					title = "Notification Feedback",
					description = "Show notifications when flip is detected or DND state changes",
					checked = notificationsEnabled,
					onCheckedChange = { viewModel.setNotificationsEnabled(it) },
				)

				val highSensitivityModeEnabled by viewModel.highSensitivityModeEnabled.collectAsState()
				SettingsSwitchItem(
					title = "High Sensitivity Mode",
					description = "When enabled, any orientation that's not face down will be considered face up",
					checked = highSensitivityModeEnabled,
					onCheckedChange = { viewModel.setHighSensitiviflip up sensitivity increased.
				)

				ListItem(
					headlineContent = { Text("Flip Sensitivity") },
					supportingContent = { Text("Adjust the sensitivity of flip detection") },
					trailingContent = {
						val flipSensitivity by viewModel.flipSensitivity.collectAsState()
						var sliderPosition by remember { mutableStateOf(flipSensitivity) }

						LaunchedEffect(flipSensitivity) {
							sliderPosition = flipSensitivity
						}

						Slider(
							value = sliderPosition,
							onValueChange = { newSensitivity ->
								val steps = listOf(0f, 0.33f, 0.66f, 1f)
								val nearestStep =
									steps.minByOrNull { kotlin.math.abs(it - newSensitivity) } ?: newSensitivity
								sliderPosition = nearestStep
							},
							onValueChangeFinished = {
								viewModel.setFlipSensitivity(sliderPosition)
							},
							modifier = Modifier.width(200.dp),
							steps = 2
						)
					}
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
													painter = painterResource(id = R.drawable.ic_radio_button_checked),
													contentDescription = "Selected",
													tint = MaterialTheme.colorScheme.primary
												)
											} else {
												Icon(
													painter = painterResource(id = R.drawable.ic_radio_button_unchecked),
													contentDescription = "Not Selected",
													tint = MaterialTheme.colorScheme.onSurfaceVariant
												)
											}
										},
										modifier = Modifier.clickable {
											viewModel.setDndOnSound(sound)
											viewModel.playSelectedSound(sound)
											dndOnExpanded = false
										}
									)
									
									// Add a button to select custom sound if CUSTOM is selected
									if (sound == Sound.CUSTOM && dndOnSound == Sound.CUSTOM) {
										val dndOnCustomSoundUri by viewModel.dndOnCustomSoundUri.collectAsState()
										Button(
											onClick = {
												val intent = Intent(context, SoundPickerActivity::class.java).apply {
													putExtra(SoundPickerActivity.EXTRA_SOUND_TYPE, SoundPickerActivity.DND_ON_SOUND)
												}
												try {
													context.startActivity(intent)
												} catch (e: ActivityNotFoundException) {
													Toast.makeText(context, "Could not open sound picker", Toast.LENGTH_SHORT).show()
												}
											},
											modifier = Modifier
												.fillMaxWidth()
												.padding(horizontal = 16.dp, vertical = 8.dp)
										) {
											Text("Select Custom Sound")
										}
										
										if (!dndOnCustomSoundUri.isNullOrEmpty()) {
											Text(
												"Custom sound selected",
												modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
												style = MaterialTheme.typography.bodyMedium
											)
										}
									}
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
													painter = painterResource(id = R.drawable.ic_radio_button_checked),
													contentDescription = "Selected",
													tint = MaterialTheme.colorScheme.primary
												)
											} else {
												Icon(
													painter = painterResource(id = R.drawable.ic_radio_button_unchecked),
													contentDescription = "Not Selected",
													tint = MaterialTheme.colorScheme.onSurfaceVariant
												)
											}
										},
										modifier = Modifier.clickable {
											viewModel.setDndOffSound(sound)
											viewModel.playSelectedSound(sound)
											dndOffExpanded = false
										}
									)
									
									// Add a button to select custom sound if CUSTOM is selected
									if (sound == Sound.CUSTOM && dndOffSound == Sound.CUSTOM) {
										val dndOffCustomSoundUri by viewModel.dndOffCustomSoundUri.collectAsState()
										Button(
											onClick = {
												val intent = Intent(context, SoundPickerActivity::class.java).apply {
													putExtra(SoundPickerActivity.EXTRA_SOUND_TYPE, SoundPickerActivity.DND_OFF_SOUND)
												}
												try {
													context.startActivity(intent)
												} catch (e: ActivityNotFoundException) {
													Toast.makeText(context, "Could not open sound picker", Toast.LENGTH_SHORT).show()
												}
											},
											modifier = Modifier
												.fillMaxWidth()
												.padding(horizontal = 16.dp, vertical = 8.dp)
										) {
											Text("Select Custom Sound")
										}
										
										if (!dndOffCustomSoundUri.isNullOrEmpty()) {
											Text(
												"Custom sound selected",
												modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
												style = MaterialTheme.typography.bodyMedium
											)
										}
									}
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
										val steps = listOf(0f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1f)
										val nearestStep =
											steps.minByOrNull { kotlin.math.abs(it - newVolume) } ?: newVolume
										sliderPosition = nearestStep
									},
									onValueChangeFinished = {
										viewModel.setCustomVolume(sliderPosition)
										// Play DND on sound as feedback when slider changes
										viewModel.playSelectedSound(viewModel.dndOnSound.value)
									},
									modifier = Modifier.width(200.dp),
									steps = 9
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
						headlineContent = { Text("DND On Vibration pattern") },
						supportingContent = {
							val dndOnVibration by viewModel.dndOnVibration.collectAsState()
							Text(dndOnVibration.displayName)
						},
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
													painter = painterResource(id = R.drawable.ic_radio_button_checked),
													contentDescription = "Selected",
													tint = MaterialTheme.colorScheme.primary
												)
											} else {
												Icon(
													painter = painterResource(id = R.drawable.ic_radio_button_unchecked),
													contentDescription = "Not Selected",
													tint = MaterialTheme.colorScheme.onSurfaceVariant
												)
											}
										},
										modifier = Modifier.clickable {
											viewModel.setDndOnVibration(pattern)
											viewModel.playSelectedVibration(pattern)
											dndOnVibrationExpanded = false
										}
									)
								}
							}
							Spacer(modifier = Modifier.height(20.dp))
						}
					}

					ListItem(
						headlineContent = { Text("DND Off Vibration pattern") },
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
													painter = painterResource(id = R.drawable.ic_radio_button_checked),
													contentDescription = "Selected",
													tint = MaterialTheme.colorScheme.primary
												)
											} else {
												Icon(
													painter = painterResource(id = R.drawable.ic_radio_button_unchecked),
													contentDescription = "Not Selected",
													tint = MaterialTheme.colorScheme.onSurfaceVariant
												)
											}
										},
										modifier = Modifier.clickable {
											viewModel.setDndOffVibration(pattern)
											viewModel.playSelectedVibration(pattern)
											dndOffVibrationExpanded = false
										}
									)
								}
							}
							Spacer(modifier = Modifier.height(20.dp))
						}
					}

					SettingsSwitchItem(
						title = "Custom Vibration strength",
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
										// Snap to nearest step (0.0, 0.33, 0.66, 1.0)
										val steps = listOf(0f, 0.33f, 0.66f, 1f)
										val nearestStep =
											steps.minByOrNull { kotlin.math.abs(it - newStrength) } ?: newStrength
										sliderPosition = nearestStep
									},
									onValueChangeFinished = {
										viewModel.setCustomVibrationStrength(sliderPosition)
										viewModel.playSelectedVibration(VibrationPattern.SINGLE_PULSE)
									},
									modifier = Modifier.width(200.dp),
									steps = 2 // This creates 4 discrete points (start, 2 steps, and end)
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
