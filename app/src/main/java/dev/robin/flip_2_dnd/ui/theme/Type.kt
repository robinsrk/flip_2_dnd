package dev.robin.flip_2_dnd.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dev.robin.flip_2_dnd.R

val PlusJakartaSans = FontFamily(
	Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
	Font(R.font.plus_jakarta_sans_medium, FontWeight.Medium),
	Font(R.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
	Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold),
	Font(R.font.plus_jakarta_sans_extrabold, FontWeight.ExtraBold),
)

// Set of Material typography styles to start with
val Typography = Typography(
	displayLarge = TextStyle(
		fontFamily = PlusJakartaSans,
		fontWeight = FontWeight.ExtraBold,
		fontSize = 57.sp,
		lineHeight = 64.sp,
		letterSpacing = (-0.25).sp
	),
	displayMedium = TextStyle(
		fontFamily = PlusJakartaSans,
		fontWeight = FontWeight.ExtraBold,
		fontSize = 45.sp,
		lineHeight = 52.sp,
		letterSpacing = 0.sp
	),
	displaySmall = TextStyle(
		fontFamily = PlusJakartaSans,
		fontWeight = FontWeight.Bold,
		fontSize = 36.sp,
		lineHeight = 44.sp,
		letterSpacing = 0.sp
	),
	headlineLarge = TextStyle(
		fontFamily = PlusJakartaSans,
		fontWeight = FontWeight.Bold,
		fontSize = 32.sp,
		lineHeight = 40.sp,
		letterSpacing = 0.sp
	),
	headlineMedium = TextStyle(
		fontFamily = PlusJakartaSans,
		fontWeight = FontWeight.Bold,
		fontSize = 28.sp,
		lineHeight = 36.sp,
		letterSpacing = 0.sp
	),
	headlineSmall = TextStyle(
		fontFamily = PlusJakartaSans,
		fontWeight = FontWeight.SemiBold,
		fontSize = 24.sp,
		lineHeight = 32.sp,
		letterSpacing = 0.sp
	),
	titleLarge = TextStyle(
		fontFamily = PlusJakartaSans,
		fontWeight = FontWeight.Bold,
		fontSize = 22.sp,
		lineHeight = 28.sp,
		letterSpacing = 0.sp
	),
	titleMedium = TextStyle(
		fontFamily = PlusJakartaSans,
		fontWeight = FontWeight.SemiBold,
		fontSize = 16.sp,
		lineHeight = 24.sp,
		letterSpacing = 0.15.sp
	),
	titleSmall = TextStyle(
		fontFamily = PlusJakartaSans,
		fontWeight = FontWeight.Medium,
		fontSize = 14.sp,
		lineHeight = 20.sp,
		letterSpacing = 0.1.sp
	),
	bodyLarge = TextStyle(
		fontFamily = PlusJakartaSans,
		fontWeight = FontWeight.Normal,
		fontSize = 16.sp,
		lineHeight = 24.sp,
		letterSpacing = 0.5.sp
	),
	bodyMedium = TextStyle(
		fontFamily = PlusJakartaSans,
		fontWeight = FontWeight.Normal,
		fontSize = 14.sp,
		lineHeight = 20.sp,
		letterSpacing = 0.25.sp
	),
	bodySmall = TextStyle(
		fontFamily = PlusJakartaSans,
		fontWeight = FontWeight.Normal,
		fontSize = 12.sp,
		lineHeight = 16.sp,
		letterSpacing = 0.4.sp
	),
	labelLarge = TextStyle(
		fontFamily = PlusJakartaSans,
		fontWeight = FontWeight.Medium,
		fontSize = 14.sp,
		lineHeight = 20.sp,
		letterSpacing = 0.1.sp
	),
	labelMedium = TextStyle(
		fontFamily = PlusJakartaSans,
		fontWeight = FontWeight.Medium,
		fontSize = 12.sp,
		lineHeight = 16.sp,
		letterSpacing = 0.5.sp
	),
	labelSmall = TextStyle(
		fontFamily = PlusJakartaSans,
		fontWeight = FontWeight.Medium,
		fontSize = 11.sp,
		lineHeight = 16.sp,
		letterSpacing = 0.5.sp
	)
)