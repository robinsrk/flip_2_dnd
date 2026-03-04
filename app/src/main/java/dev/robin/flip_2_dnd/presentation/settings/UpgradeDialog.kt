package dev.robin.flip_2_dnd.presentation.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.robin.flip_2_dnd.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpgradeDialog(
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val patreonUrl = "https://robinsrk.netlify.app/buyflip2dnd"

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = MaterialTheme.colorScheme.surface,
    tonalElevation = 8.dp,
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(24.dp)
        .padding(bottom = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Icon(
        imageVector = Icons.Default.Star,
        contentDescription = null,
        modifier = Modifier
          .size(48.dp)
          .padding(bottom = 16.dp),
        tint = MaterialTheme.colorScheme.primary
      )
      Text(
        text = stringResource(id = R.string.upgrade_to_pro),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.ExtraBold,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 16.dp)
      )
      Text(
        text = stringResource(id = R.string.upgrade_to_pro_description),
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(bottom = 24.dp)
      )

      Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 24.dp)
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          FeatureItem(stringResource(id = R.string.feature_auto_start))
          FeatureItem(stringResource(id = R.string.feature_sensitivity))
          FeatureItem(stringResource(id = R.string.feature_delay))
          FeatureItem(stringResource(id = R.string.feature_schedules))
          FeatureItem(stringResource(id = R.string.feature_custom_sounds))
          FeatureItem(stringResource(id = R.string.feature_proximity))
          FeatureItem(stringResource(id = R.string.feature_telegram))
        }
      }

      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Button(
          onClick = {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.patreon.com/posts/flip-2-dnd-150924870"))
            context.startActivity(intent)
            onDismiss()
          },
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
          )
        ) {
          Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(20.dp))
          Spacer(Modifier.width(8.dp))
          Text(
            stringResource(id = R.string.upgrade_to_pro),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
          )
        }

        TextButton(
          onClick = onDismiss,
          modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
        ) {
          Text(
            stringResource(id = R.string.maybe_later),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}

@Composable
fun FeatureItem(text: String) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Icon(
      imageVector = Icons.Default.Star,
      contentDescription = null,
      modifier = Modifier.size(16.dp),
      tint = MaterialTheme.colorScheme.primary
    )
    Text(
      text = text,
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.Medium
    )
  }
}
