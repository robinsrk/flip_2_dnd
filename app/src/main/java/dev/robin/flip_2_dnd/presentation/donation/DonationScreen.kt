package dev.robin.flip_2_dnd.presentation.donation

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
                        "Support the Developer",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    ) 
                },
				navigationIcon = {
					FilledIconButton(
						onClick = {navController?.popBackStack()},
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
                text = "Your support helps keep this app free and continuously improving!",
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
                text = "Thank you for your generous support! ❤️",
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
