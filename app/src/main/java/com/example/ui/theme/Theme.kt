package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PulseSaveFrostedLightColorScheme = lightColorScheme(
    primary = ElectricCyan,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = Color(0xFF1E1B4B),
    secondary = VividViolet,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEDE9FE),
    onSecondaryContainer = Color(0xFF2E1065),
    tertiary = EmeraldSafe,
    onTertiary = Color.White,
    background = FrostIceBlueMid,
    onBackground = TextPrimarySnow,
    surface = MidnightIndigo,
    onSurface = TextPrimarySnow,
    surfaceVariant = AbyssalBlue,
    onSurfaceVariant = TextSecondarySlate,
    error = RoseDanger,
    onError = Color.White,
    outline = GlassBorderSubtle
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PulseSaveFrostedLightColorScheme,
        typography = Typography,
        content = content
    )
}
