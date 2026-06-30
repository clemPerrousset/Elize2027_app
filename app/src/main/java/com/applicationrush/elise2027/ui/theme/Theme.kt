package com.applicationrush.elise2027.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Background = Color(0xFF0D0D1A)
val Surface = Color(0xFF16162A)
val SurfaceVariant = Color(0xFF1E1E35)
val OnSurface = Color(0xFFF0F0F8)
val OnSurfaceMuted = Color(0xFF8888AA)

private val DarkColors = darkColorScheme(
    background = Background,
    surface = Surface,
    surfaceVariant = SurfaceVariant,
    onBackground = OnSurface,
    onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceMuted,
    primary = Color(0xFF7B7BFF),
    onPrimary = Color.White,
)

@Composable
fun ElizeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        content = content,
    )
}
