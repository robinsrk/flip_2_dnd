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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LargeTopAppBar
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import android.os.Build
import android.provider.Settings
import androidx.compose.ui.platform.LocalContext
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
			LargeTopAppBar(
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
						text = stringResource(id = R.string.settings),
						style = MaterialTheme.typography.headlineSmall.copy(
							fontSize = currentFontSize,
							fontWeight = currentFontWeight
						),
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
					)
				},
				navigationIcon = {
					FilledIconButton(
						modifier = Modifier.padding(8.dp),
						onClick = { navController?.popBackStack() },
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
					.padding(horizontal = 24.dp)
					.nestedScroll(scrollBehavior.nestedScrollConnection)
		) {
			item {

				Text(
					text = stringResource(id = R.string.behavior),
					color = MaterialTheme.colorScheme.primary,
					style = MaterialTheme.typography.titleLarge.copy(
						fontWeight = FontWeight.Bold
					),
					modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
				)

				SettingsSwitchItem(
					title = stringResource(id = R.string.screen_off_only),
					description = stringResource(id = R.string.screen_off_only_description),
					checked = screenOffOnly,
					onCheckedChange = { viewModel.setScreenOffOnly(it) },
				)

				SettingsSwitchItem(
					title = stringResource(id = R.string.priority_dnd),
					description = stringResource(id = R.string.priority_dnd_description),
					checked = priorityDndEnabled,
					onCheckedChange = { viewModel.setPriorityDndEnabled(it) },
				)

				val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
				SettingsSwitchItem(
					title = stringResource(id = R.string.notifications_enabled),
					description = stringResource(id = R.string.notifications_enabled_description),
					checked = notificationsEnabled,
					onCheckedChange = { viewModel.setNotificationsEnabled(it) },
				)

				val highSensitivityModeEnabled by viewModel.highSensitivityModeEnabled.collectAsState()
				SettingsSwitchItem(
					title = stringResource(id = R.string.high_sensitivity_mode),
					description = stringResource(id = R.string.high_sensitivity_mode_description),
					checked = highSensitivityModeEnabled,
					onCheckedChange = { viewModel.setHighSensitivityModeEnabled(it) },
				)

				SettingsSliderItem(
					title = stringResource(id = R.string.flip_sensitivity),
					description = stringResource(id = R.string.flip_sensitivity_description),
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

				Spacer(modifier = Modifier.height(16.dp))

				Text(
					text = stringResource(id = R.string.general),
					color = MaterialTheme.colorScheme.primary,
					style = MaterialTheme.typography.titleLarge.copy(
						fontWeight = FontWeight.Bold
					),
					modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
				)

SettingsClickableItem(
    title = stringResource(id = R.string.language),
    description = stringResource(id = R.string.language_description),
    onClick = {
        try {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Intent(android.provider.Settings.ACTION_APP_LOCALE_SETTINGS)
            } else {
                Intent(android.provider.Settings.ACTION_LOCALE_SETTINGS)
            }
            intent.data = Uri.fromParts("package", context.packageName, null)
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                context,
                R.string.error_opening_language_settings,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
)

				Spacer(modifier = Modifier.height(16.dp))

				Text(
					text = stringResource(id = R.string.sound),
					color = MaterialTheme.colorScheme.primary,
					style = MaterialTheme.typography.titleLarge.copy(
						fontWeight = FontWeight.Bold
					),
					modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
				)

				SettingsSwitchItem(
					title = stringResource(id = R.string.sound_enabled),
					description = stringResource(id = R.string.sound_enabled_description),
					checked = soundEnabled,
					onCheckedChange = { viewModel.setSoundEnabled(it) },
				)

				if (soundEnabled) {
					var dndOnExpanded by remember { mutableStateOf(false) }
					var dndOffExpanded by remember { mutableStateOf(false) }
					val soundSheetState = rememberModalBottomSheetState()

					SettingsClickableItem(
						title = stringResource(id = R.string.dnd_on_sound),
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
						title = stringResource(id = R.string.dnd_off_sound),
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
						title = stringResource(id = R.string.use_custom_volume),
						description = stringResource(id = R.string.use_custom_volume_description),
						checked = useCustomVolume,
						onCheckedChange = { viewModel.setUseCustomVolume(it) },
					)

					if (useCustomVolume) {
						SettingsSliderItem(
							title = stringResource(id = R.string.custom_volume),
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


				// Vibration Section
				Text(
					text = stringResource(id = R.string.vibration),
					color = MaterialTheme.colorScheme.primary,
					style = MaterialTheme.typography.titleLarge.copy(
						fontWeight = FontWeight.Bold
					),
					modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
				)

				SettingsSwitchItem(
					title = stringResource(id = R.string.vibration_enabled),
					description = stringResource(id = R.string.vibration_enabled_description),
					checked = vibrationEnabled,
					onCheckedChange = { viewModel.setVibrationEnabled(it) },
				)

				if (vibrationEnabled) {
					var dndOnVibrationExpanded by remember { mutableStateOf(false) }
					var dndOffVibrationExpanded by remember { mutableStateOf(false) }
					val vibrationSheetState = rememberModalBottomSheetState()

					SettingsClickableItem(
						title = stringResource(id = R.string.dnd_on_vibration_pattern),
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
						title = stringResource(id = R.string.dnd_off_vibration_pattern),
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
						title = stringResource(id = R.string.use_custom_vibration_strength),
						description = stringResource(id = R.string.use_custom_vibration_description),
						checked = useCustomVibration,
						onCheckedChange = { viewModel.setUseCustomVibration(it) },
					)

					if (useCustomVibration) {
						SettingsSliderItem(
							title = stringResource(id = R.string.custom_vibration_strength),
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

				Text(
					text = stringResource(id = R.string.extras),
					color = MaterialTheme.colorScheme.primary,
					style = MaterialTheme.typography.titleLarge.copy(
						fontWeight = FontWeight.Bold
					),
					modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
				)

				SettingsClickableItem(
					title = stringResource(id = R.string.join_telegram),
					description = stringResource(id = R.string.join_telegram_description),
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
					title = stringResource(id = R.string.support_developer),
					description = stringResource(id = R.string.support_developer_description),
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
