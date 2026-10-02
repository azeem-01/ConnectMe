package com.ce46.connectme.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.ce46.connectme.data.ColorPalette
import com.ce46.connectme.data.ThemeMode

@Composable
fun ConnectMeTheme(
    themeMode: ThemeMode,
    useDynamicColor: Boolean,
    palette: ColorPalette,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val context = LocalContext.current
    val colorScheme = if (useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        schemeFor(palette, dark)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ConnectMeTypography,
        content = content
    )
}
