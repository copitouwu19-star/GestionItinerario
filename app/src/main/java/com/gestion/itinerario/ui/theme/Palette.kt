package com.gestion.itinerario.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/** Una paleta de colores seleccionable por el usuario, con su esquema para tema claro y oscuro. */
data class AppPalette(
    val id: String,
    val displayName: String,
    /** Cuatro tonos representativos para la muestra circular del selector. */
    val swatches: List<Color>,
    val light: ColorScheme,
    val dark: ColorScheme
)

private fun mix(from: Color, to: Color, ratio: Float): Color = Color(
    red   = from.red   + (to.red   - from.red)   * ratio,
    green = from.green + (to.green - from.green) * ratio,
    blue  = from.blue  + (to.blue  - from.blue)  * ratio,
    alpha = 1f
)

/** Genera un esquema de colores claro a partir de tres tonos semilla, conservando las superficies neutras del tema. */
private fun lightSchemeFrom(primary: Color, secondary: Color, tertiary: Color): ColorScheme = lightColorScheme(
    primary              = primary,
    onPrimary            = Color.White,
    primaryContainer     = mix(primary, Color.White, 0.82f),
    onPrimaryContainer   = primary,
    secondary            = secondary,
    onSecondary          = Color.White,
    secondaryContainer   = mix(secondary, Color.White, 0.82f),
    onSecondaryContainer = secondary,
    tertiary             = tertiary,
    onTertiary           = Color.White,
    tertiaryContainer    = mix(tertiary, Color.White, 0.82f),
    onTertiaryContainer  = tertiary,
    background           = LightBackground,
    surface              = LightSurface,
    surfaceVariant       = LightSurfaceVariant,
    onBackground         = OnLightSurface,
    onSurface            = OnLightSurface,
    onSurfaceVariant     = OnLightSurfaceVariant,
    outline              = LightOutline
)

/** Genera un esquema de colores oscuro a partir de tres tonos semilla, conservando las superficies neutras del tema. */
private fun darkSchemeFrom(primary: Color, secondary: Color, tertiary: Color): ColorScheme = darkColorScheme(
    primary              = primary,
    onPrimary            = mix(primary, Color.Black, 0.65f),
    primaryContainer     = mix(primary, Color.Black, 0.55f),
    onPrimaryContainer   = primary,
    secondary            = secondary,
    onSecondary          = mix(secondary, Color.Black, 0.65f),
    secondaryContainer   = mix(secondary, Color.Black, 0.55f),
    onSecondaryContainer = secondary,
    tertiary             = tertiary,
    onTertiary           = mix(tertiary, Color.Black, 0.65f),
    background           = DarkBackground,
    surface              = DarkSurface,
    surfaceVariant       = DarkSurfaceVariant,
    onBackground         = OnDarkSurface,
    onSurface            = OnDarkSurface,
    onSurfaceVariant     = OnDarkSurfaceVariant,
    outline              = DarkOutline
)

private fun palette(
    id: String,
    displayName: String,
    primaryLight: Color, primaryDark: Color,
    secondaryLight: Color, secondaryDark: Color,
    tertiaryLight: Color, tertiaryDark: Color
) = AppPalette(
    id = id,
    displayName = displayName,
    swatches = listOf(primaryLight, secondaryLight, tertiaryLight, primaryDark),
    light = lightSchemeFrom(primaryLight, secondaryLight, tertiaryLight),
    dark  = darkSchemeFrom(primaryDark, secondaryDark, tertiaryDark)
)

/** Paletas disponibles para que el usuario elija en Configuración → Apariencia. */
val AppPalettes: List<AppPalette> = listOf(
    AppPalette(
        id = "indigo",
        displayName = "Violeta Vibrante",
        swatches = listOf(Primary40, Secondary40, Tertiary40, Primary80),
        light = lightSchemeFrom(Primary40, Secondary40, Tertiary40),
        dark  = darkSchemeFrom(Primary80, Secondary80, Tertiary80)
    ),
    palette(
        id = "kinetic", displayName = "Kinetic Service",
        primaryLight = Color(0xFFAD1457), primaryDark = Color(0xFFF48FB1),
        secondaryLight = Color(0xFF6A1B9A), secondaryDark = Color(0xFFCE93D8),
        tertiaryLight = Color(0xFFEF6C00), tertiaryDark = Color(0xFFFFCC80)
    ),
    palette(
        id = "candy", displayName = "Candy",
        primaryLight = Color(0xFFD81B60), primaryDark = Color(0xFFF8BBD0),
        secondaryLight = Color(0xFF8E24AA), secondaryDark = Color(0xFFE1BEE7),
        tertiaryLight = Color(0xFFFF4081), tertiaryDark = Color(0xFFFFCDD2)
    ),
    palette(
        id = "glacier", displayName = "Glacier",
        primaryLight = Color(0xFF0277BD), primaryDark = Color(0xFF81D4FA),
        secondaryLight = Color(0xFF00838F), secondaryDark = Color(0xFF80DEEA),
        tertiaryLight = Color(0xFF455A64), tertiaryDark = Color(0xFFB0BEC5)
    ),
    palette(
        id = "neon_tokyo", displayName = "Neon Tokyo",
        primaryLight = Color(0xFF6200EA), primaryDark = Color(0xFFB388FF),
        secondaryLight = Color(0xFFC2185B), secondaryDark = Color(0xFFFF80AB),
        tertiaryLight = Color(0xFF00838F), tertiaryDark = Color(0xFF84FFFF)
    ),
    palette(
        id = "terra", displayName = "Terra",
        primaryLight = Color(0xFF558B2F), primaryDark = Color(0xFFAED581),
        secondaryLight = Color(0xFF6D4C41), secondaryDark = Color(0xFFBCAAA4),
        tertiaryLight = Color(0xFFF9A825), tertiaryDark = Color(0xFFFFF59D)
    ),
    palette(
        id = "sahara", displayName = "Sahara",
        primaryLight = Color(0xFFE65100), primaryDark = Color(0xFFFFCC80),
        secondaryLight = Color(0xFF6D4C41), secondaryDark = Color(0xFFD7CCC8),
        tertiaryLight = Color(0xFFF9A825), tertiaryDark = Color(0xFFFFE082)
    )
)

fun appPaletteById(id: String): AppPalette = AppPalettes.firstOrNull { it.id == id } ?: AppPalettes.first()
