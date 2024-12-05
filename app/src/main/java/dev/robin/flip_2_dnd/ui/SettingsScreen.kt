package dev.robin.flip_2_dnd.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.robin.flip_2_dnd.settings.SettingsManager

@Composable
fun SettingsScreen(
    settingsManager: SettingsManager,
    onBackClick: () -> Unit
) {
    val onlyWhenScreenOff by settingsManager.onlyWhenScreenOff.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Bar with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium
            )
            // Empty box for alignment
            Box(modifier = Modifier.size(48.dp))
        }

        // Settings items
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Only when screen off",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "DND will only toggle when the screen is off",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = onlyWhenScreenOff,
                    onCheckedChange = { settingsManager.setOnlyWhenScreenOff(it) }
                )
            }
        }
    }
}
