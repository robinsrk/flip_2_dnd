package dev.robin.flip_2_dnd.presentation.donation

import dev.robin.flip_2_dnd.BuildConfig
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.navigation.NavController
import dev.robin.flip_2_dnd.utils.copyAddressToClipboard
import dev.robin.flip_2_dnd.R
import androidx.compose.foundation.background
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationScreen(navController: NavController? = null) {
	val context = LocalContext.current
	val redotpayID = BuildConfig.REDOTPAY_ID
	val usdtAddress = BuildConfig.USDT_ADDRESS
	val btcAddress = BuildConfig.BTC_ADDRESS

	val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

	Scaffold(
		modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
		topBar = {
			LargeTopAppBar(
				title = {
					val expandedTextStyle =
						MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold)
					val collapsedTextStyle =
						MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold)

					val fraction = scrollBehavior.state.collapsedFraction
					val currentFontSize =
						lerp(expandedTextStyle.fontSize.value, collapsedTextStyle.fontSize.value, fraction).sp
					val currentFontWeight = FontWeight.ExtraBold

					Text(
						text = stringResource(id = R.string.support_developer),
						style = MaterialTheme.typography.headlineSmall.copy(
							fontSize = currentFontSize,
							fontWeight = currentFontWeight
						),
						maxLines = 2,
						overflow = TextOverflow.Ellipsis,
					)
				},
				navigationIcon = {
					IconButton(onClick = { navController?.popBackStack() }) {
						Icon(
							imageVector = Icons.AutoMirrored.Filled.ArrowBack,
							contentDescription = stringResource(id = R.string.back)
						)
					}
				},
				colors = TopAppBarDefaults.topAppBarColors(
					containerColor = MaterialTheme.colorScheme.background,
					scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
				),
				scrollBehavior = scrollBehavior
			)
		}
	) { paddingValues ->
		LazyColumn(
			modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues)
          .padding(horizontal = 24.dp),
			verticalArrangement = Arrangement.spacedBy(16.dp)
		) {
			item {
				Text(
					text = stringResource(id = R.string.your_support_helps),
					style = MaterialTheme.typography.bodyLarge,
					textAlign = TextAlign.Center,
					modifier = Modifier.padding(bottom = 16.dp).fillMaxWidth(),
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}

			item {
				DonationCard(
					title = stringResource(id = R.string.pay_with_redotpay),
					description = redotpayID,
					trailingText = stringResource(id = R.string.redotpay_id),
					onClick = { copyAddressToClipboard(context, redotpayID) },
					icon = R.drawable.ic_coin
				)
			}

			item {
				DonationCard(
					title = stringResource(id = R.string.pay_with_usdt),
					description = usdtAddress,
					trailingText = stringResource(id = R.string.usdt_network),
					onClick = { copyAddressToClipboard(context, usdtAddress) },
					icon = R.drawable.ic_coin
				)
			}

			item {
				DonationCard(
					title = stringResource(id = R.string.pay_with_bitcoin),
					description = btcAddress,
					trailingText = stringResource(id = R.string.bitcoin_network),
					onClick = { copyAddressToClipboard(context, btcAddress) },
					icon = R.drawable.ic_coin
				)
			}

			item {
				Text(
					text = stringResource(id = R.string.thanks_for_support),
					style = MaterialTheme.typography.titleLarge,
					fontWeight = FontWeight.Bold,
					textAlign = TextAlign.Center,
					modifier = Modifier.padding(top = 24.dp, bottom = 32.dp).fillMaxWidth(),
					color = MaterialTheme.colorScheme.primary
				)
			}
		}
	}
}

@Composable
fun DonationCard(
	title: String,
	description: String,
	trailingText: String,
	onClick: () -> Unit,
	icon: Int
) {
	Card(
		modifier = Modifier
			.fillMaxWidth()
			.clickable { onClick() },
		shape = RoundedCornerShape(28.dp),
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
		),
		elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
	) {
		Row(
			modifier = Modifier.padding(20.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(16.dp)
		) {
			Box(
				modifier = Modifier
					.size(48.dp)
					.clip(RoundedCornerShape(16.dp))
					.background(MaterialTheme.colorScheme.primaryContainer),
				contentAlignment = Alignment.Center
			) {
				Icon(
					painter = painterResource(id = icon),
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onPrimaryContainer,
					modifier = Modifier.size(24.dp)
				)
			}

			Column(modifier = Modifier.weight(1f)) {
				Text(
					text = title,
					style = MaterialTheme.typography.titleLarge,
					fontWeight = FontWeight.Bold
				)
				Text(
					text = description,
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)
				Spacer(modifier = Modifier.height(4.dp))
				Text(
					text = trailingText,
					style = MaterialTheme.typography.labelMedium,
					color = MaterialTheme.colorScheme.primary,
					fontWeight = FontWeight.SemiBold
				)
			}
		}
	}
}
