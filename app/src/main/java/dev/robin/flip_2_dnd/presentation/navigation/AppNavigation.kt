package dev.robin.flip_2_dnd.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.robin.flip_2_dnd.R
import dev.robin.flip_2_dnd.presentation.donation.DonationScreen
import dev.robin.flip_2_dnd.presentation.history.HistoryScreen
import dev.robin.flip_2_dnd.presentation.history.HistoryViewModel
import dev.robin.flip_2_dnd.presentation.main.MainScreen
import dev.robin.flip_2_dnd.presentation.main.MainViewModel

sealed class Screen(val route: String, val icon: Int, val labelResId: Int) {
	object Home : Screen("home", R.drawable.ic_home, R.string.home)
	object History : Screen("history", R.drawable.ic_history, R.string.history)
	object Donation : Screen("donation", R.drawable.ic_coin, R.string.support)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
	val navController = rememberNavController()

	NavHost(
		navController = navController,
		startDestination = Screen.Home.route,
		modifier = Modifier,
	) {
		composable(Screen.Home.route) {
			val mainViewModel: MainViewModel = hiltViewModel()
			val state by mainViewModel.state.collectAsState()
			MainScreen(
				state = state,
				onDonateClick = { navController.navigate(Screen.Donation.route) },
				onToggleService = { mainViewModel.toggleService() },
				onHistoryClick = { navController.navigate(Screen.History.route) }
			)
		}
		composable(Screen.History.route) {
			val historyViewModel: HistoryViewModel = hiltViewModel()
			val state by historyViewModel.state.collectAsState()
			HistoryScreen(
				state = state,
				onClearHistory = { historyViewModel.clearHistory() }
			)
		}
		composable(Screen.Donation.route) {
			DonationScreen(navController = navController)
		}
	}
}