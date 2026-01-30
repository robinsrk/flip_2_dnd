package dev.robin.flip_2_dnd.presentation.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.robin.flip_2_dnd.R

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LargeFloatingActionButton

import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.util.lerp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
	state: MainState,
	onDonateClick: () -> Unit,
	onToggleService: () -> Unit
) {
	val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

	Scaffold(
		modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
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
						text = stringResource(id = R.string.app_name),
						style = MaterialTheme.typography.headlineSmall.copy(
							fontSize = currentFontSize,
							fontWeight = currentFontWeight
						),
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
					)
				},
				colors = TopAppBarDefaults.topAppBarColors(
					containerColor = MaterialTheme.colorScheme.background,
					scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
				),
				scrollBehavior = scrollBehavior
			)
		},
		floatingActionButton = {
			LargeFloatingActionButton(
				onClick = onToggleService,
				containerColor = if (state.isServiceRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
				contentColor = if (state.isServiceRunning) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
				shape = RoundedCornerShape(28.dp)
			) {
				Icon(
					painter = painterResource(
						id = if (state.isServiceRunning) R.drawable.ic_pause else R.drawable.ic_play
					),
					contentDescription = if (state.isServiceRunning) "Stop Service" else "Start Service",
					modifier = Modifier.size(36.dp)
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
					.fillMaxWidth()
					.aspectRatio(1f)
					.padding(16.dp),
				shape = RoundedCornerShape(48.dp),
				colors = CardDefaults.cardColors(
					containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
				),
				elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
			) {
				Box(
					modifier = Modifier.fillMaxSize(),
					contentAlignment = Alignment.Center
				) {
					Image(
						painter = painterResource(
							id = if (state.isDndEnabled) R.drawable.ic_dnd_on else R.drawable.ic_dnd_off
						),
						contentDescription = "Do Not Disturb Icon",
						modifier = Modifier.fillMaxSize(0.6f),
						colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
					)
				}
			}

			Column(
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.spacedBy(8.dp)
			) {
				Text(
					text = if (state.isDndEnabled) state.dndMode.uppercase() else stringResource(id = R.string.dnd_off).uppercase(),
					style = MaterialTheme.typography.displayMedium,
					fontWeight = FontWeight.ExtraBold,
					color = MaterialTheme.colorScheme.primary,
					textAlign = TextAlign.Center
				)

				Text(
					text = if (state.isServiceRunning) stringResource(id = R.string.service_running) else stringResource(
						id = R.string.service_not_running
					),
					style = MaterialTheme.typography.titleLarge,
					fontWeight = FontWeight.Medium,
					color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
					textAlign = TextAlign.Center
				)
			}
		}
	}
}
