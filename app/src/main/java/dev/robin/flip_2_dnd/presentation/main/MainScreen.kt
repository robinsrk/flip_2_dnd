package dev.robin.flip_2_dnd.presentation.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    state: MainState,
    onSettingsClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Flip 2 DND") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Lottie Animation
            val composition by rememberLottieComposition(
                LottieCompositionSpec.Asset("animations/flip_animation.json")
            )
            
            var isForward by remember { mutableStateOf(true) }
            
            LaunchedEffect(composition) {
                while (true) {
                    delay(2000) // Wait for animation to complete
                    isForward = !isForward
                }
            }
            
            val progress by animateLottieCompositionAsState(
                composition = composition,
                isPlaying = true,
                iterations = 1,
                speed = if (isForward) 1f else -1f,
                restartOnPlay = false
            )
            
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (state.isScreenOffOnly) {
                    "Screen will turn off when phone is flipped"
                } else {
                    "DND will be enabled when phone is flipped"
                },
                style = MaterialTheme.typography.titleMedium
            )

            if (state.isVibrationEnabled) {
                Text(
                    text = "Vibration enabled",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (state.isSoundEnabled) {
                Text(
                    text = "Sound enabled",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
