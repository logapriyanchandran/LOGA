package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NavyPrimary,
    secondary = MaritimeSecondary,
    tertiary = TradeGreenAccent,
    background = DeepOceanBackground,
    surface = SlateContainer,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = LightGrayText,
    onSurface = LightGrayText,
    surfaceVariant = SlateCard
)

private val LightColorScheme = lightColorScheme(
    primary = NavyPrimary,
    secondary = MaritimeSecondary,
    tertiary = TradeGreenAccent,
    background = DeepOceanBackground,
    surface = SlateContainer,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = LightGrayText,
    onSurface = LightGrayText,
    surfaceVariant = SlateCard
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Use our gorgeous Professional Polish light B2B theme by default!
    dynamicColor: Boolean = false, // Use our handcrafted design palettes instead of system defaults
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
