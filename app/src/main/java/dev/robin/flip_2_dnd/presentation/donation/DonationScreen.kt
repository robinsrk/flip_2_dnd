package dev.robin.flip_2_dnd.presentation.donation

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
            TopAppBar(
                title = { Text("Support the Developer") },
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
                ListItem(
                    headlineContent = { 
                        Text(
                            "Pay with RedotPay",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    supportingContent = { Text(redotpayID) },
                    trailingContent = { 
                        Text(
                            "RedotPay ID",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    leadingContent = {
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
                ListItem(
                    headlineContent = { 
                        Text(
                            "Pay with USDT",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    supportingContent = { Text(usdtAddress) },
                    trailingContent = { 
                        Text("BNB Smart Chain (BEP20)",
                            
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    leadingContent = {
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
                ListItem(
                    headlineContent = { 
                        Text(
                            "Pay with ETH",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    supportingContent = { Text(usdtAddress) },
                    trailingContent = { 
                        Text(
                            "BNB Smart Chain (BEP20)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    leadingContent = {
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
                ListItem(
                    headlineContent = { 
                        Text(
                            "Pay with BTC",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    supportingContent = { Text(btcAddress) },
                    trailingContent = { 
                        Text(
                            "Bitcoin",
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    leadingContent = {
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
