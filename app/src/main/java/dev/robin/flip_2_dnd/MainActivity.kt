package dev.robin.flip_2_dnd

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.robin.flip_2_dnd.presentation.main.MainScreen
import dev.robin.flip_2_dnd.presentation.main.MainViewModel
import dev.robin.flip_2_dnd.presentation.settings.SettingsScreen
import dev.robin.flip_2_dnd.services.FlipDetectorService
import dev.robin.flip_2_dnd.ui.theme.Flip_2_DNDTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        startFlipDetectorService()

        setContent {
            Flip_2_DNDTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "main"
                ) {
                    composable("main") {
                        val state by mainViewModel.state.collectAsState()
                        MainScreen(
                            state = state,
                            onSettingsClick = {
                                navController.navigate("settings")
                            }
                        )
                    }
                    composable("settings") {
                        SettingsScreen(
                            navController = navController
                        )
                    }
                }
            }
        }
    }

    private fun startFlipDetectorService() {
        Intent(this, FlipDetectorService::class.java).also { intent ->
            startForegroundService(intent)
        }
    }
}