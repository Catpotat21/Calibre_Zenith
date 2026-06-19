package com.example.calibre_zenith.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val ThemeBg = Color(0xFF0D0D12)
val SurfaceCard = Color(0xFF161622)
val SurfacePressed = Color(0xFF222232)
val AccentNeonGreen = Color(0xFF00FFCC)
val TextPrimary = Color(0xFFF3F4F6)
val TextSecondary = Color(0xFF9CA3AF)
val TextMuted = Color(0xFF4B5563)

private val CyberPunkColorScheme = darkColorScheme(
    background = ThemeBg,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    primary = AccentNeonGreen,
    secondary = TextSecondary,
    onSecondary = TextMuted
)

@Composable
fun CalibreZenithTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CyberPunkColorScheme,
        content = content
    )
}