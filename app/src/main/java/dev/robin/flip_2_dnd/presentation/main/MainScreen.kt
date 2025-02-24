package dev.robin.flip_2_dnd.presentation.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
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
            TopAppBar(
                title = { 
                    Text(
                        "Flip 2 DND",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    ) 
                },
                actions = {
                    IconButton(onClick = onDonateClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_coin),
                            contentDescription = "Support"
                        )
                    }
                }
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
                text = if (state.isDndEnabled) "DND: ${state.dndMode}" else "DND is OFF",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (state.isServiceRunning) "Service is running" else "Service is stopped",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
