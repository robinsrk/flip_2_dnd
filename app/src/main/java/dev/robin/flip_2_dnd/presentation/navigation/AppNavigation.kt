package dev.robin.flip_2_dnd.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import dev.robin.flip_2_dnd.R
import dev.robin.flip_2_dnd.presentation.donation.DonationScreen
import dev.robin.flip_2_dnd.presentation.main.MainScreen
import dev.robin.flip_2_dnd.presentation.main.MainState
import dev.robin.flip_2_dnd.presentation.main.MainViewModel
import dev.robin.flip_2_dnd.presentation.settings.SettingsScreen

sealed class Screen(val route: String, val icon: Int, val label: String) {
    object Home : Screen("home", R.drawable.ic_home, "Home")
    object Settings : Screen("settings", R.drawable.ic_settings, "Settings")
    object Donation : Screen("donation", R.drawable.ic_coin, "Support")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = listOf(Screen.Home, Screen.Settings)

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(painter = painterResource(screen.icon), contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
        ) {
            composable(Screen.Home.route) {
                val mainViewModel: MainViewModel = hiltViewModel()
                val state by mainViewModel.state.collectAsState()
                MainScreen(
                    state = state,
                    onDonateClick = { navController.navigate(Screen.Donation.route) },
                    onToggleService = { mainViewModel.toggleService() }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    navController = navController,
                    onDonateClick = { navController.navigate(Screen.Donation.route) }
                )
            }
            composable(Screen.Donation.route) {
                DonationScreen(navController = navController)
            }
        }
    }
}