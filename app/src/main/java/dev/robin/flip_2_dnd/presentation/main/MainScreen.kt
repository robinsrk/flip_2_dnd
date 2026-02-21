package dev.robin.flip_2_dnd.presentation.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dev.robin.flip_2_dnd.R
import dev.robin.flip_2_dnd.presentation.settings.SettingsContent
import dev.robin.flip_2_dnd.presentation.settings.SettingsViewModel
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
	state: MainState,
	onDonateClick: () -> Unit,
	onToggleService: () -> Unit,
	onHistoryClick: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val dynamicPeekHeight = screenHeight / 3

	val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded
    )
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = sheetState
    )

	BottomSheetScaffold(
	    scaffoldState = scaffoldState,
		modifier = Modifier,
		sheetPeekHeight = dynamicPeekHeight,
        sheetContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        sheetTonalElevation = 8.dp,
        sheetSwipeEnabled = true,
		topBar = {
			TopAppBar(
				title = {
					Text(
						text = stringResource(id = R.string.app_name),
						style = MaterialTheme.typography.headlineLarge,
						fontWeight = FontWeight.ExtraBold,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
					)
				},
                actions = {
                    IconButton(onClick = onHistoryClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_history),
                            contentDescription = stringResource(id = R.string.history),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
				colors = TopAppBarDefaults.topAppBarColors(
					containerColor = MaterialTheme.colorScheme.background,
				)
			)
		},
        sheetContent = {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.75f) 
                    .padding(bottom = 24.dp)
            ) {
                SettingsContent(
                    viewModel = settingsViewModel,
                    onDonateClick = onDonateClick
                )
            }
        }
	) { paddingValues ->
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(paddingValues)
				.padding(horizontal = 24.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically)
		) {
			Card(
				modifier = Modifier
					.size(200.dp)
					.aspectRatio(1f)
					.padding(16.dp),
				shape = CircleShape,
				colors = CardDefaults.cardColors(
					containerColor = if (state.isServiceRunning) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
				),
				elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
				onClick = onToggleService
			) {
				Box(
					modifier = Modifier.fillMaxSize(),
					contentAlignment = Alignment.Center
				) {
					Icon(
						imageVector = Icons.Default.ScreenRotation,
						contentDescription = if (state.isServiceRunning) stringResource(id = R.string.stop_service) else stringResource(id = R.string.start_service),
						modifier = Modifier.fillMaxSize(0.4f),
						tint = if (state.isServiceRunning) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
					)
				}
			}

			Column(
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.spacedBy(8.dp)
			) {
				Text(
					text = if (state.isServiceRunning) stringResource(id = R.string.service_running).uppercase() else stringResource(id = R.string.service_not_running).uppercase(),
					style = MaterialTheme.typography.headlineMedium,
					fontWeight = FontWeight.ExtraBold,
					color = if (state.isServiceRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
					textAlign = TextAlign.Center
				)
				
				if (state.isServiceRunning) {
				    Text(
					    text = stringResource(id = state.dndMode),
					    style = MaterialTheme.typography.titleMedium,
					    fontWeight = FontWeight.Medium,
					    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
					    textAlign = TextAlign.Center
				    )
				}
			}
            
            Spacer(modifier = Modifier.height(32.dp))
		}
	}
}
