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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.alpha
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
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.Spacer
import dev.robin.flip_2_dnd.R
import dev.robin.flip_2_dnd.utils.getFileNameFromUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
	viewModel: SettingsViewModel = hiltViewModel(),
	navController: NavController? = null,
	onDonateClick: () -> Unit,
) {
	val context = LocalContext.current
	val clipboardManager = LocalClipboardManager.current
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
	val hasSecureSettingsPermission by viewModel.hasSecureSettingsPermission.collectAsState()
	val headphoneDetectionEnabled by viewModel.headphoneDetectionEnabled.collectAsState()

	val dndScheduleEnabled by viewModel.dndScheduleEnabled.collectAsState()
	val dndScheduleStartTime by viewModel.dndScheduleStartTime.collectAsState()
	val dndScheduleEndTime by viewModel.dndScheduleEndTime.collectAsState()
	val dndScheduleDays by viewModel.dndScheduleDays.collectAsState()

	val soundScheduleEnabled by viewModel.soundScheduleEnabled.collectAsState()
	val soundScheduleStartTime by viewModel.soundScheduleStartTime.collectAsState()
	val soundScheduleEndTime by viewModel.soundScheduleEndTime.collectAsState()
	val soundScheduleDays by viewModel.soundScheduleDays.collectAsState()

	val vibrationScheduleEnabled by viewModel.vibrationScheduleEnabled.collectAsState()
	val vibrationScheduleStartTime by viewModel.vibrationScheduleStartTime.collectAsState()
	val vibrationScheduleEndTime by viewModel.vibrationScheduleEndTime.collectAsState()
	val vibrationScheduleDays by viewModel.vibrationScheduleDays.collectAsState()

	var showAdbDialog by remember { mutableStateOf(false) }
	var showUpgradeDialog by remember { mutableStateOf(false) }
	var showChangelogSheet by remember { mutableStateOf(false) }
	val changelogSheetState = rememberModalBottomSheetState()

	if (showUpgradeDialog) {
		UpgradeDialog(onDismiss = { showUpgradeDialog = false })
	}

	val packageInfo = remember {
		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
				context.packageManager.getPackageInfo(
					context.packageName,
					android.content.pm.PackageManager.PackageInfoFlags.of(0)
				)
			} else {
				@Suppress("DEPRECATION")
				context.packageManager.getPackageInfo(context.packageName, 0)
			}
		} catch (e: Exception) {
			null
		}
	}

	val versionName = packageInfo?.versionName ?: "Unknown"
	val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
		packageInfo?.longVersionCode ?: 0L
	} else {
		@Suppress("DEPRECATION")
		packageInfo?.versionCode?.toLong() ?: 0L
	}

	val isBeta = versionName.contains("pre", ignoreCase = true) || 
				 versionName.contains("beta", ignoreCase = true)

	val changelogText = remember(versionName) {
		try {
			val rawContent = context.resources.openRawResource(R.raw.changelog).bufferedReader().use { it.readText() }
			val sections = rawContent.split("\n\n").filter { it.isNotBlank() }
			
			sections.filter { section ->
				val firstLine = section.lines().firstOrNull() ?: ""
				val isSectionBeta = firstLine.contains("(BETA)", ignoreCase = true)
				
				if (isSectionBeta) {
					isBeta // Only include beta sections if current app is beta
				} else {
					true // Always include stable sections
				}
			}.joinToString("\n\n")
		} catch (e: Exception) {
			"Unable to load changelog"
		}
	}

	val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

	Scaffold(
		topBar = {
			LargeTopAppBar(
				title = {
					val expandedTextStyle =
						MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold)
					val collapsedTextStyle =
						MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold)


					val fraction = scrollBehavior.state.collapsedFraction
					val currentFontSize =
						lerp(expandedTextStyle.fontSize.value, collapsedTextStyle.fontSize.value, fraction).sp
					val currentFontWeight = FontWeight.ExtraBold

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
					IconButton(onClick = { navController?.popBackStack() }) {
						Icon(
							imageVector = Icons.AutoMirrored.Filled.ArrowBack,
							contentDescription = "Back"
						)
					}
				},
				colors = TopAppBarDefaults.topAppBarColors(
					containerColor = MaterialTheme.colorScheme.background,
					scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
				),
				scrollBehavior = scrollBehavior
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
				Column {
					Text(
						text = stringResource(id = R.string.behavior),
						color = MaterialTheme.colorScheme.primary,
						style = MaterialTheme.typography.headlineSmall,
						fontWeight = FontWeight.ExtraBold,
						modifier = Modifier.padding(top = 24.dp, bottom = 8.dp, start = 8.dp),
					)

					val autoStartEnabled by viewModel.autoStartEnabled.collectAsState()
					SettingsSwitchItem(
						title = stringResource(id = R.string.auto_start),
						description = stringResource(id = R.string.auto_start_description),
						checked = autoStartEnabled,
						onCheckedChange = {
							if (dev.robin.flip_2_dnd.PremiumProvider.engine.autoStartEnabled()) {
								viewModel.setAutoStartEnabled(it)
							} else {
								showUpgradeDialog = true
							}
						},
						alpha = if (dev.robin.flip_2_dnd.PremiumProvider.engine.autoStartEnabled()) 1f else 0.5f,
						isPro = true
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
						onCheckedChange = {
							if (dev.robin.flip_2_dnd.PremiumProvider.engine.advancedSensitivityEnabled()) {
								viewModel.setHighSensitivityModeEnabled(it)
							} else {
								showUpgradeDialog = true
							}
						},
						alpha = if (dev.robin.flip_2_dnd.PremiumProvider.engine.advancedSensitivityEnabled()) 1f else 0.5f,
						isPro = true
					)

					val batterySaverOnFlipEnabled by viewModel.batterySaverOnFlipEnabled.collectAsState()
					SettingsSwitchItem(
						title = stringResource(id = R.string.battery_saver),
						description = stringResource(id = R.string.battery_saver_description),
						checked = batterySaverOnFlipEnabled,
						onCheckedChange = {
							if (dev.robin.flip_2_dnd.PremiumProvider.engine.batterySaverSyncEnabled()) {
								if (hasSecureSettingsPermission) {
									viewModel.setBatterySaverOnFlipEnabled(it)
								} else {
									showAdbDialog = true
								}
							} else {
								showUpgradeDialog = true
							}
						},
						alpha = if (dev.robin.flip_2_dnd.PremiumProvider.engine.batterySaverSyncEnabled() && hasSecureSettingsPermission) 1f else 0.5f,
						isPro = true
					)

					if (showAdbDialog) {
						AlertDialog(
							onDismissRequest = { showAdbDialog = false },
							title = { Text(stringResource(R.string.adb_permission_required)) },
							text = {
								Column {
									Text(stringResource(R.string.adb_command_description))
									Spacer(modifier = Modifier.height(16.dp))
									Text(
										text = stringResource(R.string.adb_command_text),
										style = MaterialTheme.typography.bodySmall,
										modifier = Modifier
											.fillMaxWidth()
											.background(
												MaterialTheme.colorScheme.surfaceVariant,
												RoundedCornerShape(8.dp)
											)
											.padding(12.dp),
										textAlign = TextAlign.Start,
										fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
									)
								}
							},
							confirmButton = {
								TextButton(onClick = {
									clipboardManager.setText(AnnotatedString(context.getString(R.string.adb_command_text)))
									Toast.makeText(context, "Command copied to clipboard", Toast.LENGTH_SHORT).show()
									showAdbDialog = false
								}) {
									Text(stringResource(R.string.copy_command))
								}
							},
							dismissButton = {
								TextButton(onClick = { showAdbDialog = false }) {
									Text(stringResource(R.string.close))
								}
							}
						)
					}

					val activationDelay by viewModel.activationDelay.collectAsState()
					SettingsSliderItem(
						title = stringResource(id = R.string.activation_delay),
						description = stringResource(id = R.string.activation_delay_description),
						isPro = true,
						sliderContent = {
							var sliderPosition by remember { mutableStateOf(activationDelay.toFloat()) }
							LaunchedEffect(activationDelay) {
								sliderPosition = activationDelay.toFloat()
							}
							Column {
								Slider(
									value = sliderPosition,
									onValueChange = { 
										if (dev.robin.flip_2_dnd.PremiumProvider.engine.delayCustomizationEnabled()) {
											sliderPosition = it 
										} else {
											showUpgradeDialog = true
										}
									},
									onValueChangeFinished = {
										if (dev.robin.flip_2_dnd.PremiumProvider.engine.delayCustomizationEnabled()) {
											viewModel.setActivationDelay(sliderPosition.toInt())
										}
									},
									valueRange = 0f..10f,
									steps = 9,
									modifier = Modifier.width(200.dp),
									enabled = dev.robin.flip_2_dnd.PremiumProvider.engine.delayCustomizationEnabled()
								)
								Text(
									text = stringResource(id = R.string.seconds, sliderPosition.toInt()),
									style = MaterialTheme.typography.bodySmall,
									color = MaterialTheme.colorScheme.onSurfaceVariant,
									modifier = Modifier
										.padding(start = 8.dp)
										.alpha(if (dev.robin.flip_2_dnd.PremiumProvider.engine.delayCustomizationEnabled()) 1f else 0.5f)
								)
							}
						}
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

					ScheduleSection(
						title = stringResource(id = R.string.dnd_activation_schedule),
						enabled = dndScheduleEnabled,
						onEnabledChange = { 
							if (dev.robin.flip_2_dnd.PremiumProvider.engine.scheduleEnabled()) {
								viewModel.setDndScheduleEnabled(it) 
							} else {
								showUpgradeDialog = true
							}
						},
						description = stringResource(id = R.string.dnd_schedule_description),
						startTime = dndScheduleStartTime,
						onStartTimeChange = { viewModel.setDndScheduleStartTime(it) },
						endTime = dndScheduleEndTime,
						onEndTimeChange = { viewModel.setDndScheduleEndTime(it) },
						selectedDays = dndScheduleDays,
						onDaysChange = { viewModel.setDndScheduleDays(it) },
						alpha = if (dev.robin.flip_2_dnd.PremiumProvider.engine.scheduleEnabled()) 1f else 0.5f,
						isPro = true
					)
				}
			}

    item {
        Column {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(id = R.string.detection),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp, start = 8.dp),
            )

            val flashlightDetectionEnabled by viewModel.flashlightDetectionEnabled.collectAsState()
            SettingsSwitchItem(
                title = stringResource(id = R.string.flashlight_detection),
                description = stringResource(id = R.string.flashlight_detection_description),
                checked = flashlightDetectionEnabled,
                onCheckedChange = { 
                    if (dev.robin.flip_2_dnd.PremiumProvider.engine.detectionFiltersEnabled()) {
                        viewModel.setFlashlightDetectionEnabled(it) 
                    } else {
                        showUpgradeDialog = true
                    }
                },
                alpha = if (dev.robin.flip_2_dnd.PremiumProvider.engine.detectionFiltersEnabled()) 1f else 0.5f,
                isPro = true
            )

            val mediaPlaybackDetectionEnabled by viewModel.mediaPlaybackDetectionEnabled.collectAsState()
            SettingsSwitchItem(
                title = stringResource(id = R.string.media_playback_detection),
                description = stringResource(id = R.string.media_playback_detection_description),
                checked = mediaPlaybackDetectionEnabled,
                onCheckedChange = { 
                    if (dev.robin.flip_2_dnd.PremiumProvider.engine.detectionFiltersEnabled()) {
                        viewModel.setMediaPlaybackDetectionEnabled(it) 
                    } else {
                        showUpgradeDialog = true
                    }
                },
                alpha = if (dev.robin.flip_2_dnd.PremiumProvider.engine.detectionFiltersEnabled()) 1f else 0.5f,
                isPro = true
            )

            val headphoneDetectionEnabled by viewModel.headphoneDetectionEnabled.collectAsState()
            SettingsSwitchItem(
                title = stringResource(id = R.string.headphone_detection),
                description = stringResource(id = R.string.headphone_detection_description),
                checked = headphoneDetectionEnabled,
                onCheckedChange = { 
                    if (dev.robin.flip_2_dnd.PremiumProvider.engine.detectionFiltersEnabled()) {
                        viewModel.setHeadphoneDetectionEnabled(it) 
                    } else {
                        showUpgradeDialog = true
                    }
                },
                alpha = if (dev.robin.flip_2_dnd.PremiumProvider.engine.detectionFiltersEnabled()) 1f else 0.5f,
                isPro = true
            )
        }
    }

    item {
        Column {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(id = R.string.general),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp, start = 8.dp),
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
        }
    }

    item {
        Column {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(id = R.string.sound),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp, start = 8.dp),
            )

            SettingsSwitchItem(
                title = stringResource(id = R.string.sound_enabled),
                description = stringResource(id = R.string.sound_enabled_description),
                checked = soundEnabled,
                onCheckedChange = { viewModel.setSoundEnabled(it) },
            )
        }
    }

    item {
        if (soundEnabled) {
            Column {
                var dndOnExpanded by remember { mutableStateOf(false) }
                var dndOffExpanded by remember { mutableStateOf(false) }
                val soundSheetState = rememberModalBottomSheetState()

                val dndOnCustomSoundUri by viewModel.dndOnCustomSoundUri.collectAsState()
                val dndOnCustomSoundName = remember(dndOnCustomSoundUri) {
                    dndOnCustomSoundUri?.let { uriString ->
                        getFileNameFromUri(context, Uri.parse(uriString))
                    } ?: "None selected"
                }

                SettingsClickableItem(
                    title = stringResource(id = R.string.dnd_on_sound),
                    description = if (dndOnSound == Sound.CUSTOM) "Custom: $dndOnCustomSoundName" else dndOnSound.name,
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
										title = if (sound == Sound.CUSTOM) "Custom Sound" else sound.name,
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
											if (sound == Sound.CUSTOM) {
												if (dev.robin.flip_2_dnd.PremiumProvider.engine.customSoundEnabled()) {
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
												} else {
													showUpgradeDialog = true
												}
											} else {
												viewModel.setDndOnSound(sound)
												viewModel.playSelectedSound(sound)
											}
											dndOnExpanded = false
										}
									)
								}
							}
							Spacer(modifier = Modifier.height(20.dp))
						}
					}

					val dndOffCustomSoundUri by viewModel.dndOffCustomSoundUri.collectAsState()
					val dndOffCustomSoundName = remember(dndOffCustomSoundUri) {
						dndOffCustomSoundUri?.let { uriString ->
							getFileNameFromUri(context, Uri.parse(uriString))
						} ?: "None selected"
					}

					SettingsClickableItem(
						title = stringResource(id = R.string.dnd_off_sound),
						description = if (dndOffSound == Sound.CUSTOM) "Custom: $dndOffCustomSoundName" else dndOffSound.name,
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
										title = if (sound == Sound.CUSTOM) "Custom Sound" else sound.name,
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
											if (sound == Sound.CUSTOM) {
												if (dev.robin.flip_2_dnd.PremiumProvider.engine.customSoundEnabled()) {
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
												} else {
													showUpgradeDialog = true
												}
											} else {
												viewModel.setDndOffSound(sound)
												viewModel.playSelectedSound(sound, isForDndOn = false)
											}
											dndOffExpanded = false
										}
									)
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

					Spacer(modifier = Modifier.height(12.dp))

					ScheduleSection(
						title = null,
						enabled = soundScheduleEnabled,
						onEnabledChange = { 
							if (dev.robin.flip_2_dnd.PremiumProvider.engine.scheduleEnabled()) {
								viewModel.setSoundScheduleEnabled(it) 
							} else {
								showUpgradeDialog = true
							}
						},
						description = stringResource(id = R.string.sound_schedule_description),
						startTime = soundScheduleStartTime,
						onStartTimeChange = { viewModel.setSoundScheduleStartTime(it) },
						endTime = soundScheduleEndTime,
						onEndTimeChange = { viewModel.setSoundScheduleEndTime(it) },
						selectedDays = soundScheduleDays,
						onDaysChange = { viewModel.setSoundScheduleDays(it) },
						alpha = if (dev.robin.flip_2_dnd.PremiumProvider.engine.scheduleEnabled()) 1f else 0.5f
					)
				}
			}
		}

	item {
		Column {
			// Vibration Section
			Text(
				text = stringResource(id = R.string.vibration),
				color = MaterialTheme.colorScheme.primary,
				style = MaterialTheme.typography.headlineSmall,
				fontWeight = FontWeight.ExtraBold,
				modifier = Modifier.padding(top = 24.dp, bottom = 8.dp, start = 8.dp),
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

				Spacer(modifier = Modifier.height(12.dp))

				ScheduleSection(
					title = null,
					enabled = vibrationScheduleEnabled,
					onEnabledChange = { 
						if (dev.robin.flip_2_dnd.PremiumProvider.engine.scheduleEnabled()) {
							viewModel.setVibrationScheduleEnabled(it) 
						} else {
							showUpgradeDialog = true
						}
					},
					description = stringResource(id = R.string.vibration_schedule_description),
					startTime = vibrationScheduleStartTime,
					onStartTimeChange = { viewModel.setVibrationScheduleStartTime(it) },
					endTime = vibrationScheduleEndTime,
					onEndTimeChange = { viewModel.setVibrationScheduleEndTime(it) },
					selectedDays = vibrationScheduleDays,
					onDaysChange = { viewModel.setVibrationScheduleDays(it) },
					alpha = if (dev.robin.flip_2_dnd.PremiumProvider.engine.scheduleEnabled()) 1f else 0.5f
				)
			}
		}
	}

	item {
		Column {
			Text(
				text = stringResource(id = R.string.extras),
				color = MaterialTheme.colorScheme.primary,
				style = MaterialTheme.typography.headlineSmall,
				fontWeight = FontWeight.ExtraBold,
				modifier = Modifier.padding(top = 24.dp, bottom = 8.dp, start = 8.dp),
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

			SettingsClickableItem(
				title = "Version",
				description = "$versionName ($versionCode)",
				leadingIcon = {
					Icon(
						imageVector = Icons.Default.Info,
						contentDescription = "Version Icon",
						tint = MaterialTheme.colorScheme.primary
					)
				},
				onClick = { showChangelogSheet = true }
			)

			if (showChangelogSheet) {
				ModalBottomSheet(
					onDismissRequest = { showChangelogSheet = false },
					sheetState = changelogSheetState,
					containerColor = MaterialTheme.colorScheme.surface,
					tonalElevation = 8.dp
				) {
					Column(
						modifier = Modifier
							.fillMaxWidth()
							.padding(24.dp)
					) {
						Text(
							text = "Changelog",
							style = MaterialTheme.typography.headlineSmall,
							fontWeight = FontWeight.Bold,
							color = MaterialTheme.colorScheme.primary
						)
						Spacer(modifier = Modifier.height(16.dp))
						Text(
							text = changelogText,
							style = MaterialTheme.typography.bodyLarge,
							lineHeight = 24.sp
						)
						Spacer(modifier = Modifier.height(24.dp))
					}
				}
			}
		}
	}
}
}
}

@Composable
private fun ScheduleSection(
    title: String? = null,
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    description: String,
    startTime: String,
    onStartTimeChange: (String) -> Unit,
    endTime: String,
    onEndTimeChange: (String) -> Unit,
    selectedDays: Set<Int>,
    onDaysChange: (Set<Int>) -> Unit,
    alpha: Float = 1f,
    isPro: Boolean = false
) {
    val context = LocalContext.current

    Column(modifier = Modifier.alpha(alpha)) {
        if (title != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (isPro) {
                    ProBadge(modifier = Modifier.padding(start = 8.dp))
                }
            }
        }

        SettingsSwitchItem(
            title = stringResource(id = R.string.schedule_enabled),
            description = description,
            checked = enabled,
            onCheckedChange = onEnabledChange,
        )

        if (enabled) {
            SettingsClickableItem(
                title = stringResource(id = R.string.start_time),
                description = startTime,
                onClick = {
                    val parts = startTime.split(":")
                    val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
                    val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
                    TimePickerDialog(
                        context,
                        { _, h, m ->
                            onStartTimeChange(String.format("%02d:%02d", h, m))
                        },
                        hour,
                        minute,
                        true
                    ).show()
                }
            )

            SettingsClickableItem(
                title = stringResource(id = R.string.end_time),
                description = endTime,
                onClick = {
                    val parts = endTime.split(":")
                    val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
                    val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
                    TimePickerDialog(
                        context,
                        { _, h, m ->
                            onEndTimeChange(String.format("%02d:%02d", h, m))
                        },
                        hour,
                        minute,
                        true
                    ).show()
                }
            )

            Text(
                text = stringResource(id = R.string.days),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            DayPicker(
                selectedDays = selectedDays,
                onDaysChange = onDaysChange
            )
        }
    }
}

@Composable
private fun DayPicker(
    selectedDays: Set<Int>,
    onDaysChange: (Set<Int>) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")
        daysOfWeek.forEachIndexed { index, day ->
            val dayValue = index + 1
            val isSelected = selectedDays.contains(dayValue)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable {
                        val newDays = if (isSelected) {
                            selectedDays - dayValue
                        } else {
                            selectedDays + dayValue
                        }
                        onDaysChange(newDays)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
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
	enabled: Boolean = true,
	alpha: Float = 1f,
	isPro: Boolean = false
) {
	Card(
		shape = RoundedCornerShape(28.dp),
		colors = CardDefaults.cardColors(
			containerColor = if (checked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceContainerHigh
		),
		elevation = CardDefaults.cardElevation(
			defaultElevation = 0.dp
		),
		modifier = Modifier
			.fillMaxWidth()
			.padding(vertical = 4.dp)
			.clip(RoundedCornerShape(28.dp))
			.clickable(enabled = enabled) { onCheckedChange(!checked) }
			.alpha(alpha)
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 20.dp, vertical = 20.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(16.dp)
		) {
			Column(
				modifier = Modifier.weight(1f)
			) {
				Row(verticalAlignment = Alignment.CenterVertically) {
					Text(
						text = title,
						style = MaterialTheme.typography.titleLarge,
						fontWeight = FontWeight.Bold,
						modifier = Modifier.weight(1f, fill = false)
					)
					if (isPro) {
						ProBadge(modifier = Modifier.padding(start = 8.dp))
					}
				}
				if (description.isNotEmpty()) {
					Spacer(modifier = Modifier.height(4.dp))
					Text(
						text = description,
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
					)
				}
			}
			Switch(
				checked = checked,
				onCheckedChange = onCheckedChange,
				enabled = enabled
			)
		}
	}
}

@Composable
fun SettingsSliderItem(
	title: String,
	description: String? = null,
	isPro: Boolean = false,
	sliderContent: @Composable () -> Unit
) {
	Card(
		shape = RoundedCornerShape(28.dp),
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
		),
		elevation = CardDefaults.cardElevation(
			defaultElevation = 0.dp
		),
		modifier = Modifier
			.fillMaxWidth()
			.padding(vertical = 4.dp)
			.clip(RoundedCornerShape(28.dp))
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 20.dp, vertical = 20.dp)
		) {
			Row(verticalAlignment = Alignment.CenterVertically) {
				Text(
					text = title,
					style = MaterialTheme.typography.titleLarge,
					fontWeight = FontWeight.Bold,
					modifier = Modifier.weight(1f, fill = false)
				)
				if (isPro) {
					ProBadge(modifier = Modifier.padding(start = 8.dp))
				}
			}
			if (description != null) {
				Spacer(modifier = Modifier.height(4.dp))
				Text(
					text = description,
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
				)
			}
			Spacer(modifier = Modifier.height(12.dp))
			sliderContent()
		}
	}
}

@Composable
fun SettingsClickableItem(
	title: String,
	description: String? = null,
	onClick: () -> Unit,
	leadingIcon: (@Composable () -> Unit)? = null,
	trailingIcon: (@Composable () -> Unit)? = null,
	enabled: Boolean = true,
	alpha: Float = 1f,
	isPro: Boolean = false
) {
	Card(
		shape = RoundedCornerShape(28.dp),
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
		),
		elevation = CardDefaults.cardElevation(
			defaultElevation = 0.dp
		),
		modifier = Modifier
			.fillMaxWidth()
			.padding(vertical = 4.dp)
			.clip(RoundedCornerShape(28.dp))
			.clickable(enabled = enabled, onClick = onClick)
			.alpha(alpha)
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 20.dp, vertical = 20.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(16.dp)
		) {
			if (leadingIcon != null) {
				Box(modifier = Modifier.size(24.dp)) {
					leadingIcon()
				}
			}
			Column(
				modifier = Modifier.weight(1f)
			) {
				Row(verticalAlignment = Alignment.CenterVertically) {
					Text(
						text = title,
						style = MaterialTheme.typography.titleLarge,
						fontWeight = FontWeight.Bold,
						modifier = Modifier.weight(1f, fill = false)
					)
					if (isPro) {
						ProBadge(modifier = Modifier.padding(start = 8.dp))
					}
				}
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

@Composable
fun ProBadge(modifier: Modifier = Modifier) {
	Surface(
		modifier = modifier,
		shape = RoundedCornerShape(8.dp),
		color = MaterialTheme.colorScheme.primaryContainer,
		contentColor = MaterialTheme.colorScheme.onPrimaryContainer
	) {
		Text(
			text = "PRO",
			style = MaterialTheme.typography.labelSmall,
			fontWeight = FontWeight.ExtraBold,
			modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
		)
	}
}
