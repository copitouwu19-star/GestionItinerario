package com.gestion.itinerario.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun GestionItinerarioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    paletteId: String = "indigo",
    content: @Composable () -> Unit
) {
    val palette = appPaletteById(paletteId)
    val colorScheme = if (darkTheme) palette.dark else palette.light
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
