package dev.robin.flip_2_dnd.presentation.donation

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.robin.flip_2_dnd.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationScreen(
	navController: NavController? = null
) {
	val context = LocalContext.current
	val usdtAddress = "0xA11C1eD5213705517E050DB075D1D238e21f5D15"
	val btcAddress = "1DxZPSf4xraev8S3mJFtA4mH7QEeZHzDdQ"
	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text("Donation") },
				navigationIcon = {
					if (navController != null) {
						IconButton(onClick = { navController.popBackStack() }) {
							Icon(
								painter = painterResource(id = R.drawable.ic_arrow_back),
								contentDescription = "Back"
							)
						}
					}
				}
			)
		}
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(it)
				.padding(16.dp),
			verticalArrangement = Arrangement.Top,
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			ListItem(
				modifier = Modifier.clickable {
					val binancePayUrl = "https://app.binance.com/en/wallet"

					val intent = Intent(Intent.ACTION_VIEW, Uri.parse(binancePayUrl))
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
					try {
						context.startActivity(intent)
					} catch (e: ActivityNotFoundException) {
						// Fallback to the browser if the app is not installed
						val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(binancePayUrl))
						webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
						context.startActivity(webIntent)
					}
				},
				supportingContent = {
					Text("Binance ID")
				},
				headlineContent = {
					Text("Pay with Binance")
				},
				trailingContent = {
					Text("754979664")
				}
			)
			ListItem(
				modifier = Modifier.clickable {
					val clipboardManager =
						context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
					val clip = ClipData.newPlainText("Crypto Address", usdtAddress)
					clipboardManager.setPrimaryClip(clip)

					// Show a toast message
					Toast.makeText(context, "Address copied to clipboard", Toast.LENGTH_SHORT).show()
				},
				supportingContent = {
					Text("BNB Smart Chain(BEP20)")
				},
				headlineContent = {
					Text("Pay with USDT")
				},
				trailingContent = {
					Text(usdtAddress)
				}
			)
			ListItem(
				modifier = Modifier.clickable {
					val clipboardManager =
						context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
					val clip = ClipData.newPlainText("Crypto Address", usdtAddress)
					clipboardManager.setPrimaryClip(clip)

					// Show a toast message
					Toast.makeText(context, "Address copied to clipboard", Toast.LENGTH_SHORT).show()
				},
				supportingContent = {
					Text("BNB Smart Chain(BEP20)")
				},
				headlineContent = {
					Text("Pay with ETH")
				},
				trailingContent = {
					Text(usdtAddress)
				}
			)
			ListItem(
				modifier = Modifier.clickable {
					val clipboardManager =
						context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
					val clip = ClipData.newPlainText("Crypto Address", usdtAddress)
					clipboardManager.setPrimaryClip(clip)

					// Show a toast message
					Toast.makeText(context, "Address copied to clipboard", Toast.LENGTH_SHORT).show()
				},
				supportingContent = {
					Text("Bitcoin")
				},
				headlineContent = {
					Text("Pay with BTC")
				},
				trailingContent = {
					Text(btcAddress)
				}
			)
			Text("Thank you for your donation!")
		}
	}
}