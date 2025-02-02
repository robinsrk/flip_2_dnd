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
    val usdtAddress = "0xA11C1eD5213705517E050DB075D1D238e21f5D15"
    val btcAddress = "1DxZPSf4xraev8S3mJFtA4mH7QEeZHzDdQ"

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
                    .clickable {
                        val binancePayUrl = "https://app.binance.com/en/wallet"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(binancePayUrl))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        try {
                            context.startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(binancePayUrl))
                            webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(webIntent)
                        }
                    }
            ) {
                ListItem(
                    headlineContent = { 
                        Text(
                            "Pay with Binance",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    supportingContent = { Text("Binance ID") },
                    trailingContent = { 
                        Text(
                            "754979664",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    leadingContent = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_coin),
                            contentDescription = "Binance Icon",
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
                    supportingContent = { Text("BNB Smart Chain (BEP20)") },
                    trailingContent = { 
                        Text(
                            usdtAddress,
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
                    supportingContent = { Text("BNB Smart Chain (BEP20)") },
                    trailingContent = { 
                        Text(
                            usdtAddress,
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
                    supportingContent = { Text("Bitcoin") },
                    trailingContent = { 
                        Text(
                            btcAddress,
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
