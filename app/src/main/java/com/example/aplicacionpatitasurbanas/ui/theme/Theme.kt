package com.example.aplicacionpatitasurbanas.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    // Si ya tienes tus colores, déjalos; lo importante es no redefinir Typography aquí
)

private val DarkColors = darkColorScheme(
    // Colores dark si los usas
)

@Composable
fun PatitasurbanasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // usa la de Type.kt
        content = content
    )
}