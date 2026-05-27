package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val RadioCyan = Color(0xFF00E5FF)
val RadioLightCyan = Color(0xFF33EBFF)
val RadioDarkCyan = Color(0xFF00838F)
val BackgroundDark = Color(0xFF0C0C0D)
val SurfaceDark = Color(0xFF161618)
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFF909094)

private val DarkColorScheme = darkColorScheme(
    primary = RadioCyan,
    secondary = RadioLightCyan,
    background = BackgroundDark,
    surface = SurfaceDark,
    onPrimary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force standard elegant dark mode for music dashboard
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
