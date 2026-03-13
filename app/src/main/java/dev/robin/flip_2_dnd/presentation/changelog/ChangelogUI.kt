package dev.robin.flip_2_dnd.presentation.changelog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ChangelogAccordion(entries: List<ChangelogEntry>) {
    // Avoid nesting scrollable components. Do not use LazyColumn here since the
    // parent container already provides scrolling (outer LazyColumn in Settings).
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        entries.forEach { entry ->
            ChangelogCard(entry)
        }
    }
}

@Composable
fun ChangelogCard(entry: ChangelogEntry) {
    var expanded by remember { mutableStateOf(true) }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .clickable { expanded = !expanded }
                    .fillMaxWidth()
                    .padding(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "${entry.version} ${entry.emoji}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = if (expanded) "▴" else "▾",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                for (change in entry.changes) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text = "• ", fontSize = 14.sp, modifier = Modifier.padding(end = 6.dp))
                        Text(text = change, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}
