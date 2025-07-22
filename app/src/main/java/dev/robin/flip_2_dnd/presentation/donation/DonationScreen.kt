package dev.robin.flip_2_dnd.presentation.donation

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import copyAddressToClipboard
import dev.robin.flip_2_dnd.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationScreen(navController: NavController? = null) {
	val context = LocalContext.current
	val redotpayID = "1986637347"
	val usdtAddress = "0x9fC1AcF713A474e5317473A7fbcd7774E2fCF7C5"
	val btcAddress = "12jF3RASnsPMzvDYVKavGrmNZkMUPHJrgq"

	Scaffold(
		topBar = {
			LargeTopAppBar(
				title = {
					Text(
						stringResource(id = R.string.support_developer),
						style = MaterialTheme.typography.headlineLarge.copy(
							fontWeight = FontWeight.Bold
						)
					)
				},
				navigationIcon = {
					FilledIconButton(
						onClick = { navController?.popBackStack() },
						colors = IconButtonDefaults.filledIconButtonColors(
							containerColor = MaterialTheme.colorScheme.primary,
							contentColor = MaterialTheme.colorScheme.onPrimary
						)
					) {
						Icon(
							imageVector = Icons.AutoMirrored.Filled.ArrowBack,
							contentDescription = "Back"
						)
					}
				},
				modifier = Modifier.padding(horizontal = 8.dp) // Added padding
			)
		}
	) { paddingValues ->
		Column(
			modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues)
          .padding(16.dp),
			verticalArrangement = Arrangement.spacedBy(16.dp),
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			Text(
				text = stringResource(id = R.string.your_support_helps),
				style = MaterialTheme.typography.bodyLarge,
				textAlign = TextAlign.Center,
				modifier = Modifier.padding(bottom = 8.dp)
			)

			ElevatedCard(
				modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { copyAddressToClipboard(context, redotpayID) }
			) {
				DonationItem(
					title = "Pay with RedotPay",
					description = redotpayID,
					trailingText = "RedotPay ID",
					leadingIcon = {
						Icon(
							painter = painterResource(id = R.drawable.ic_coin),
							contentDescription = "RedotPay Icon",
							tint = MaterialTheme.colorScheme.primary
						)
					}
				)
			}

			ElevatedCard(
				modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { copyAddressToClipboard(context, usdtAddress) }
			) {
				DonationItem(
					title = "Pay with USDT",
					description = usdtAddress,
					trailingText = "BNB Smart Chain (BEP20)",
					trailingTextStyle = MaterialTheme.typography.bodySmall,
					leadingIcon = {
						Icon(
							painter = painterResource(id = R.drawable.ic_coin),
							contentDescription = "USDT Icon",
							tint = MaterialTheme.colorScheme.primary
						)
					}
				)
			}

			ElevatedCard(
				modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { copyAddressToClipboard(context, usdtAddress) }
			) {
				DonationItem(
					title = "Pay with ETH",
					description = usdtAddress,
					trailingText = "BNB Smart Chain (BEP20)",
					trailingTextStyle = MaterialTheme.typography.bodySmall,
					leadingIcon = {
						Icon(
							painter = painterResource(id = R.drawable.ic_coin),
							contentDescription = "ETH Icon",
							tint = MaterialTheme.colorScheme.primary
						)
					}
				)
			}

			ElevatedCard(
				modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { copyAddressToClipboard(context, btcAddress) }
			) {
				DonationItem(
					title = "Pay with BTC",
					description = btcAddress,
					trailingText = "Bitcoin",
					trailingTextStyle = MaterialTheme.typography.bodySmall,
					leadingIcon = {
						Icon(
							painter = painterResource(id = R.drawable.ic_coin),
							contentDescription = "BTC Icon",
							tint = MaterialTheme.colorScheme.primary
						)
					}
				)
			}

			Text(
				text = stringResource(id = R.string.thanks_for_support),
				style = MaterialTheme.typography.titleMedium,
				textAlign = TextAlign.Center,
				modifier = Modifier.padding(top = 16.dp)
			)
		}
	}
}

@Composable
fun DonationItem(
	title: String,
	description: String,
	trailingText: String? = null,
	trailingTextStyle: TextStyle = MaterialTheme.typography.bodyLarge,
	leadingIcon: @Composable (() -> Unit)? = null
) {
	Row(
		modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		if (leadingIcon != null) {
			Box(modifier = Modifier.padding(end = 16.dp)) {
				leadingIcon()
			}
		}

		Column(modifier = Modifier.weight(1f)) {
			Text(
				text = title,
				style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
			)
			Spacer(modifier = Modifier.height(4.dp))
			Text(
				text = description,
				style = MaterialTheme.typography.bodyMedium
			)
		}

		if (trailingText != null) {
			Spacer(modifier = Modifier.width(16.dp))
			Text(
				text = trailingText,
				style = trailingTextStyle
			)
		}
	}
}
