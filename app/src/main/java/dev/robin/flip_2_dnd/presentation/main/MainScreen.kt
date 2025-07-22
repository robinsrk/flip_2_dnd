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
import androidx.compose.ui.unit.dp
import dev.robin.flip_2_dnd.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
	state: MainState,
	onDonateClick: () -> Unit,
	onToggleService: () -> Unit
) {
	Scaffold(
		topBar = {
			LargeTopAppBar(
				title = {
					Text(
						stringResource(id = R.string.app_name),
						style = MaterialTheme.typography.headlineLarge.copy(
							fontWeight = FontWeight.Bold
						),
						modifier = Modifier.padding(start = 8.dp) // Added padding
					)
				},
				Modifier.padding(horizontal = 8.dp) // Added padding
			)
		},
		floatingActionButton = {
			FloatingActionButton(
				onClick = onToggleService,
				containerColor = MaterialTheme.colorScheme.primary,
				contentColor = MaterialTheme.colorScheme.onPrimary
			) {
				Icon(
					painter = painterResource(
						id = if (state.isServiceRunning) R.drawable.ic_pause else R.drawable.ic_play
					),
					contentDescription = if (state.isServiceRunning) "Stop Service" else "Start Service",
				)
			}
		}
	) { paddingValues ->
		Column(
			modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues)
          .padding(16.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Center
		) {
			Image(
				painter = painterResource(
					id = if (state.isDndEnabled) R.drawable.ic_dnd_on else R.drawable.ic_dnd_off
				),
				contentDescription = "Do Not Disturb Icon",
				modifier = Modifier
            .fillMaxWidth(0.7f)
            .aspectRatio(1f),
				colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
			)

			Spacer(modifier = Modifier.height(32.dp))

			Text(
				text = if (state.isDndEnabled) "DND: ${state.dndMode}" else stringResource(id = R.string.dnd_off),
				style = MaterialTheme.typography.headlineMedium,
				color = MaterialTheme.colorScheme.primary
			)

			Spacer(modifier = Modifier.height(16.dp))

			Text(
				text = if (state.isServiceRunning) stringResource(id = R.string.service_running) else stringResource(
					id = R.string.service_not_running
				),
				style = MaterialTheme.typography.bodyLarge,
				color = MaterialTheme.colorScheme.secondary
			)
		}
	}
}
