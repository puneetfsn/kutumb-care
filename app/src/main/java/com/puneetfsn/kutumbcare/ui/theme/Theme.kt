package com.puneetfsn.kutumbcare.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Typography
import com.puneetfsn.kutumbcare.data.ThemeMode

val Forest = Color(0xFF134E4A)
val ForestDeep = Color(0xFF0E1714)
val Saffron = Color(0xFFC45C26)
val Cream = Color(0xFFF6F1E8)
val CreamCard = Color(0xFFFFFBF4)
val Ink = Color(0xFF1C2321)
val Mist = Color(0xFF5C6561)
val Verified = Color(0xFF0F7A5A)
val AlertAmber = Color(0xFFB45309)
val GoldSoft = Color(0xFFD4A373)

private val LightColors = lightColorScheme(
    primary = Forest,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD7EBE4),
    onPrimaryContainer = Forest,
    secondary = Saffron,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF6E3D4),
    onSecondaryContainer = Color(0xFF5A2E12),
    tertiary = Verified,
    background = Cream,
    onBackground = Ink,
    surface = CreamCard,
    onSurface = Ink,
    surfaceVariant = Color(0xFFECE4D6),
    onSurfaceVariant = Mist,
    outline = Color(0xFFD5CBBA),
    error = Color(0xFFB42318)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8ED3C5),
    onPrimary = ForestDeep,
    primaryContainer = Color(0xFF1B3F3A),
    onPrimaryContainer = Color(0xFFD7EBE4),
    secondary = Color(0xFFE0A078),
    onSecondary = ForestDeep,
    secondaryContainer = Color(0xFF5A2E12),
    onSecondaryContainer = Color(0xFFF6E3D4),
    tertiary = Color(0xFF7DCEA0),
    background = ForestDeep,
    onBackground = Color(0xFFE8EFEA),
    surface = Color(0xFF16211D),
    onSurface = Color(0xFFE8EFEA),
    surfaceVariant = Color(0xFF24312C),
    onSurfaceVariant = Color(0xFFB7C2BC),
    outline = Color(0xFF3A4A44),
    error = Color(0xFFFFB4AB)
)

val KutumbTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 34.sp,
        lineHeight = 40.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 34.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp
    )
)

@Composable
fun KutumbCareTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = KutumbTypography,
        content = content
    )
}
