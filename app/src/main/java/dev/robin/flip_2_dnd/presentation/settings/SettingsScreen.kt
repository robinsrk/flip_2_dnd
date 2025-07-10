package dev.robin.flip_2_dnd.presentation.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
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
	val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

	Scaffold(
		topBar = {
			MediumTopAppBar(
				title = {
					val expandedTextStyle =
						MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
					val collapsedTextStyle =
						MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)


					val fraction = scrollBehavior.state.collapsedFraction
					val currentFontSize =
						lerp(expandedTextStyle.fontSize.value, collapsedTextStyle.fontSize.value, fraction).sp
					val currentFontWeight =
						if (fraction < 0.5f) FontWeight.Bold else FontWeight.Bold // Changed to FontWeight.Bold

					Text(
						text = "Settings",
						style = MaterialTheme.typography.headlineSmall.copy(
							fontSize = currentFontSize,
							fontWeight = currentFontWeight
						),
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
						modifier = Modifier.padding(start = 8.dp) // Added padding
					)
				},
				navigationIcon = {
					FilledIconButton(
						onClick = { navController?.popBackStack() },
						modifier = Modifier.padding(start = 16.dp),
						colors = IconButtonDefaults.filledIconButtonColors(
							containerColor = MaterialTheme.colorScheme.primary,
							contentColor = MaterialTheme.colorScheme.onPrimary
						)
					) {
						Icon(
							imageVector = Icons.AutoMirrored.Filled.ArrowBack,
							contentDescription = "Back"
						)
					}
				},
				colors = TopAppBarDefaults.topAppBarColors(
					containerColor = Color.Transparent,
					scrolledContainerColor = Color.Transparent
				),
				scrollBehavior = scrollBehavior,
				modifier = Modifier.padding(horizontal = 8.dp) // Added padding
			)
		},
	) { paddingValues ->
		LazyColumn(
			modifier =
				Modifier
					.fillMaxSize()
					.padding(paddingValues)
					.padding(horizontal = 32.dp)
					.nestedScroll(scrollBehavior.nestedScrollConnection)
		) {
			item {

				Text(
					text = "Behavior",
					color = MaterialTheme.colorScheme.primary,
					style = MaterialTheme.typography.titleLarge.copy(
						fontWeight = FontWeight.Bold
					),
					modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
				)

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
					onCheckedChange = { viewModel.setHighSensitivityModeEnabled(it) },
				)

				SettingsSliderItem(
					title = "Flip Sensitivity",
					description = "Adjust the sensitivity of flip detection",
					sliderContent = {
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

				Divider(modifier = Modifier.padding(vertical = 16.dp))

				Text(
					text = "Sound",
					color = MaterialTheme.colorScheme.primary,
					style = MaterialTheme.typography.titleLarge.copy(
						fontWeight = FontWeight.Bold
					),
					modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
				)

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

					SettingsClickableItem(
						title = "DND On Sound",
						description = dndOnSound.name,
						trailingIcon = {
							Icon(Icons.Default.ArrowDropDown, "Select sound")
						},
						onClick = { dndOnExpanded = true }
					)

					if (dndOnExpanded) {
						ModalBottomSheet(
							onDismissRequest = { dndOnExpanded = false },
							sheetState = soundSheetState
						) {
							Column {
								viewModel.availableSounds.forEach { sound ->
									SettingsClickableItem(
										title = sound.name,
										trailingIcon = {
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
										onClick = {
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
													putExtra(
														SoundPickerActivity.EXTRA_SOUND_TYPE,
														SoundPickerActivity.DND_ON_SOUND
													)
												}
												try {
													context.startActivity(intent)
												} catch (e: ActivityNotFoundException) {
													Toast.makeText(context, "Could not open sound picker", Toast.LENGTH_SHORT)
														.show()
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

					SettingsClickableItem(
						title = "DND Off Sound",
						description = dndOffSound.name,
						trailingIcon = {
							Icon(Icons.Default.ArrowDropDown, "Select sound")
						},
						onClick = { dndOffExpanded = true }
					)

					if (dndOffExpanded) {
						ModalBottomSheet(
							onDismissRequest = { dndOffExpanded = false },
							sheetState = soundSheetState
						) {
							Column {
								viewModel.availableSounds.forEach { sound ->
									SettingsClickableItem(
										title = sound.name,
										trailingIcon = {
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
										onClick = {
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
													putExtra(
														SoundPickerActivity.EXTRA_SOUND_TYPE,
														SoundPickerActivity.DND_OFF_SOUND
													)
												}
												try {
													context.startActivity(intent)
												} catch (e: ActivityNotFoundException) {
													Toast.makeText(context, "Could not open sound picker", Toast.LENGTH_SHORT)
														.show()
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
						SettingsSliderItem(
							title = "Sound Volume",
							sliderContent = {
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

				Divider(modifier = Modifier.padding(vertical = 16.dp))

				// Vibration Section
				Text(
					text = "Vibration",
					color = MaterialTheme.colorScheme.primary,
					style = MaterialTheme.typography.titleLarge.copy(
						fontWeight = FontWeight.Bold
					),
					modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
				)

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

					SettingsClickableItem(
						title = "DND On Vibration pattern",
						description = viewModel.dndOnVibration.collectAsState().value.displayName,
						trailingIcon = {
							Icon(Icons.Default.ArrowDropDown, "Select vibration pattern")
						},
						onClick = { dndOnVibrationExpanded = true }
					)

					if (dndOnVibrationExpanded) {
						ModalBottomSheet(
							onDismissRequest = { dndOnVibrationExpanded = false },
							sheetState = vibrationSheetState
						) {
							Column {
								viewModel.availableVibrationPatterns.forEach { pattern ->
									SettingsClickableItem(
										title = pattern.displayName,
										trailingIcon = {
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
										onClick = {
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

					SettingsClickableItem(
						title = "DND Off Vibration pattern",
						description = viewModel.dndOffVibration.collectAsState().value.displayName,
						trailingIcon = {
							Icon(Icons.Default.ArrowDropDown, "Select vibration pattern")
						},
						onClick = { dndOffVibrationExpanded = true }
					)

					if (dndOffVibrationExpanded) {
						ModalBottomSheet(
							onDismissRequest = { dndOffVibrationExpanded = false },
							sheetState = vibrationSheetState
						) {
							Column {
								viewModel.availableVibrationPatterns.forEach { pattern ->
									SettingsClickableItem(
										title = pattern.displayName,
										trailingIcon = {
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
										onClick = {
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
						SettingsSliderItem(
							title = "Vibration Strength",
							sliderContent = {
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
				Divider(modifier = Modifier.padding(vertical = 16.dp))

				Text(
					text = "Extras",
					color = MaterialTheme.colorScheme.primary,
					style = MaterialTheme.typography.titleLarge.copy(
						fontWeight = FontWeight.Bold
					),
					modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
				)

				SettingsClickableItem(
					title = "Join Telegram",
					description = "Join our Telegram community",
					leadingIcon = {
						Icon(
							painter = painterResource(id = R.drawable.telegram),
							contentDescription = "Telegram Icon",
							tint = MaterialTheme.colorScheme.primary,
							modifier = Modifier.width(24.dp)
						)
					},
					onClick = {
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

				SettingsClickableItem(
					title = "Support Developer",
					description = "Help keep this app free and improving",
					leadingIcon = {
						Icon(
							painter = painterResource(id = R.drawable.ic_coin),
							contentDescription = "Support Icon",
							tint = MaterialTheme.colorScheme.primary
						)
					},
					onClick = onDonateClick
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
	Card(
		shape = RoundedCornerShape(16.dp),
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surfaceContainer
		),
		elevation = CardDefaults.cardElevation(
			defaultElevation = 1.dp
		),
		modifier = Modifier
			.fillMaxWidth()
			.padding(vertical = 6.dp)
			.clip(RoundedCornerShape(16.dp))
			.clickable { onCheckedChange(!checked) }
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 16.dp, vertical = 12.dp)
		) {
			androidx.compose.foundation.layout.Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
				horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
			) {
				Column(
					modifier = Modifier.weight(1f)
				) {
					Text(
						text = title,
						style = MaterialTheme.typography.titleMedium,
					)
					Spacer(modifier = Modifier.height(4.dp))
					Text(
						text = description,
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
					)
				}
				Switch(
					checked = checked,
					onCheckedChange = onCheckedChange,
				)
			}
		}
	}
}

@Composable
fun SettingsSliderItem(
	title: String,
	description: String? = null,
	sliderContent: @Composable () -> Unit
) {
	Card(
		shape = RoundedCornerShape(16.dp),
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surfaceContainer
		),
		elevation = CardDefaults.cardElevation(
			defaultElevation = 1.dp
		),
		modifier = Modifier
			.fillMaxWidth()
			.padding(vertical = 6.dp)
			.clip(RoundedCornerShape(16.dp))
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 16.dp, vertical = 12.dp)
		) {
			androidx.compose.foundation.layout.Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
				horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
			) {
				Column(
					modifier = Modifier.weight(1f)
				) {
					Text(
						text = title,
						style = MaterialTheme.typography.titleMedium,
					)
					if (description != null) {
						Spacer(modifier = Modifier.height(4.dp))
						Text(
							text = description,
							style = MaterialTheme.typography.bodyMedium,
							color = MaterialTheme.colorScheme.onSurfaceVariant,
						)
					}
				}
				sliderContent()
			}
		}
	}
}

@Composable
fun SettingsClickableItem(
	title: String,
	description: String? = null,
	leadingIcon: @Composable (() -> Unit)? = null,
	trailingIcon: @Composable (() -> Unit)? = null,
	onClick: () -> Unit
) {
	Card(
		shape = RoundedCornerShape(16.dp),
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surfaceContainer
		),
		elevation = CardDefaults.cardElevation(
			defaultElevation = 1.dp
		),
		modifier = Modifier
			.fillMaxWidth()
			.padding(vertical = 6.dp)
			.clip(RoundedCornerShape(16.dp))
			.clickable(onClick = onClick)
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 16.dp, vertical = 12.dp)
		) {
			androidx.compose.foundation.layout.Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
			) {
				if (leadingIcon != null) {
					leadingIcon()
					Spacer(modifier = Modifier.width(16.dp))
				}
				Column(
					modifier = Modifier.weight(1f)
				) {
					Text(
						text = title,
						style = MaterialTheme.typography.titleMedium,
					)
					if (description != null) {
						Spacer(modifier = Modifier.height(4.dp))
						Text(
							text = description,
							style = MaterialTheme.typography.bodyMedium,
							color = MaterialTheme.colorScheme.onSurfaceVariant,
						)
					}
				}
				if (trailingIcon != null) {
					trailingIcon()
				}
			}
		}
	}
}
