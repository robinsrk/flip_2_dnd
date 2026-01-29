package dev.robin.flip_2_dnd.presentation.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun UpgradeDialog(
	onDismiss: () -> Unit
) {
	val context = LocalContext.current
	val gumroadUrl = "https://robinsrk.gumroad.com/l/flip2dndpro"

	AlertDialog(
		onDismissRequest = onDismiss,
		title = { Text(text = "Upgrade to Flip 2 DND Pro") },
		text = {
			Text(
				text = "Get all premium features:\n" +
						"• Auto start on boot\n" +
						"• Advanced sensitivity settings\n" +
						"• Custom activation delay\n" +
						"• DND, Sound, and Vibration schedules\n" +
						"• Custom sounds from files"
			)
		},
		confirmButton = {
			TextButton(
				onClick = {
					val intent = Intent(Intent.ACTION_VIEW, Uri.parse(gumroadUrl))
					context.startActivity(intent)
					onDismiss()
				}
			) {
				Text("Buy Pro")
			}
		},
		dismissButton = {
			TextButton(onClick = onDismiss) {
				Text("Maybe Later")
			}
		}
	)
}
