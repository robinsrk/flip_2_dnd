package dev.robin.flip_2_dnd.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
	primary = PrimaryDark,
	onPrimary = OnPrimaryDark,
	primaryContainer = PrimaryContainerDark,
	onPrimaryContainer = OnPrimaryContainerDark,
	secondary = SecondaryDark,
	onSecondary = OnSecondaryDark,
	secondaryContainer = SecondaryContainerDark,
	onSecondaryContainer = OnSecondaryContainerDark,
	tertiary = TertiaryDark,
	onTertiary = OnTertiaryDark,
	tertiaryContainer = TertiaryContainerDark,
	onTertiaryContainer = OnTertiaryContainerDark,
	error = ErrorDark,
	onError = OnErrorDark,
	errorContainer = ErrorContainerDark,
	onErrorContainer = OnErrorContainerDark,
	background = BackgroundDark,
	onBackground = OnBackgroundDark,
	surface = SurfaceDark,
	onSurface = OnSurfaceDark,
)

private val LightColorScheme = lightColorScheme(
	primary = PrimaryLight,
	onPrimary = OnPrimaryLight,
	primaryContainer = PrimaryContainerLight,
	onPrimaryContainer = OnPrimaryContainerLight,
	secondary = SecondaryLight,
	onSecondary = OnSecondaryLight,
	secondaryContainer = SecondaryContainerLight,
	onSecondaryContainer = OnSecondaryContainerLight,
	tertiary = TertiaryLight,
	onTertiary = OnTertiaryLight,
	tertiaryContainer = TertiaryContainerLight,
	onTertiaryContainer = OnTertiaryContainerLight,
	error = ErrorLight,
	onError = OnErrorLight,
	errorContainer = ErrorContainerLight,
	onErrorContainer = OnErrorContainerLight,
	background = BackgroundLight,
	onBackground = OnBackgroundLight,
	surface = SurfaceLight,
	onSurface = OnSurfaceLight,
)

@Composable
fun Flip_2_DNDTheme(
	darkTheme: Boolean = isSystemInDarkTheme(),
	// Dynamic color is available on Android 12+
	dynamicColor: Boolean = true,
	content: @Composable () -> Unit
) {
	val colorScheme = when {
		dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
			val context = LocalContext.current
			if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
		}

		darkTheme -> DarkColorScheme
		else -> LightColorScheme
	}

	MaterialTheme(
		colorScheme = colorScheme,
		typography = Typography,
		content = content
	)
}