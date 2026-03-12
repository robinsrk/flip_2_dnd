package dev.robin.flip_2_dnd.presentation.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.robin.flip_2_dnd.R
import dev.robin.flip_2_dnd.presentation.changelog.ChangelogAccordion
import dev.robin.flip_2_dnd.presentation.changelog.changelogEntries
// Reading of raw changelog is handled in the new Compose-based UI data source

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangelogBottomSheet(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Use a Compose-based changelog UI instead of HTML rendering

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(id = R.string.whats_new),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
            ) {
                // Collapsible, card-like changelog entries
                ChangelogAccordion(entries = changelogEntries)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
            ) {
                Text(
                    text = stringResource(id = R.string.continue_button),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
