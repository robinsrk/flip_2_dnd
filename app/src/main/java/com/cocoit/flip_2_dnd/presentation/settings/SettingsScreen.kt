package com.cocoit.flip_2_dnd.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cocoit.flip_2_dnd.presentation.main.MainState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: MainState,
    onBackClick: () -> Unit,
    onScreenOffOnlyChange: (Boolean) -> Unit,
    onVibrationChange: (Boolean) -> Unit,
    onSoundChange: (Boolean) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Screen off only toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Only when screen off",
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = state.isScreenOffOnly,
                    onCheckedChange = onScreenOffOnlyChange
                )
            }

            // Vibration toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Vibrate on change",
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = state.isVibrationEnabled,
                    onCheckedChange = onVibrationChange
                )
            }

            // Sound toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Play sound on change",
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = state.isSoundEnabled,
                    onCheckedChange = onSoundChange
                )
            }
        }
    }
}
