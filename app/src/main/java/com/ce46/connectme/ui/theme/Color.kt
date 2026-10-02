package com.ce46.connectme.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.ce46.connectme.data.ColorPalette

private fun palette(light: Boolean, seed: Color, container: Color, onContainer: Color) =
    if (light) {
        lightColorScheme(
            primary = seed,
            onPrimary = Color.White,
            primaryContainer = container,
            onPrimaryContainer = onContainer,
            secondary = seed,
            secondaryContainer = container,
            background = Color(0xFFF7F4F0),
            surface = Color(0xFFF7F4F0),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFF1EEEA),
            surfaceContainer = Color(0xFFEBE8E4),
            surfaceContainerHigh = Color(0xFFE5E2DE)
        )
    } else {
        darkColorScheme(
            primary = container,
            onPrimary = onContainer,
            primaryContainer = seed,
            onPrimaryContainer = Color.White,
            secondary = container,
            background = Color(0xFF121416),
            surface = Color(0xFF121416),
            surfaceContainerLowest = Color(0xFF0C0E10),
            surfaceContainerLow = Color(0xFF1A1C1E),
            surfaceContainer = Color(0xFF1E2022),
            surfaceContainerHigh = Color(0xFF282A2C)
        )
    }

fun schemeFor(palette: ColorPalette, dark: Boolean) = when (palette) {
    ColorPalette.MINT -> palette(
        !dark, Color(0xFF0F766E), Color(0xFFCCFBF1), Color(0xFF134E4A)
    )
    ColorPalette.SKY -> palette(
        !dark, Color(0xFF0369A1), Color(0xFFE0F2FE), Color(0xFF0C4A6E)
    )
    ColorPalette.CORAL -> palette(
        !dark, Color(0xFFC2410C), Color(0xFFFFEDD5), Color(0xFF7C2D12)
    )
    ColorPalette.GRAPE -> palette(
        !dark, Color(0xFF6D28D9), Color(0xFFEDE9FE), Color(0xFF4C1D95)
    )
    ColorPalette.SUNSET -> palette(
        !dark, Color(0xFFB45309), Color(0xFFFEF3C7), Color(0xFF78350F)
    )
}
