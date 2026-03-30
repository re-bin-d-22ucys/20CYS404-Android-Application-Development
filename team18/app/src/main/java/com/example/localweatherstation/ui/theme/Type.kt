package com.example.localweatherstation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.localweatherstation.ui.theme.*

private val LightColorScheme = lightColorScheme(
    primary = md_theme_primary,
    onPrimary = md_theme_onPrimary,
    secondary = md_theme_secondary,
    onSecondary = md_theme_onSecondary,
    surface = white,
    onSurface = Color.Black
)

private val DarkColorScheme = darkColorScheme(
    primary = md_theme_primary,
    onPrimary = md_theme_onPrimary,
    secondary = md_theme_secondary,
    onSecondary = md_theme_onSecondary,
    surface = Color.Black,
    onSurface = Color.White
)

@Composable
fun LocalWeatherStationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
