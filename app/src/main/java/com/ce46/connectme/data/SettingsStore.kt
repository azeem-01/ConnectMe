package com.ce46.connectme.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsStore(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val _state = MutableStateFlow(read())
    val state: StateFlow<UserSettings> = _state.asStateFlow()

    private fun read(): UserSettings = UserSettings(
        autoLoginEnabled = prefs.getBoolean(KEY_AUTO, false),
        useDynamicColor = prefs.getBoolean(KEY_DYNAMIC, true),
        palette = runCatching {
            ColorPalette.valueOf(prefs.getString(KEY_PALETTE, ColorPalette.MINT.name)!!)
        }.getOrDefault(ColorPalette.MINT),
        themeMode = runCatching {
            ThemeMode.valueOf(prefs.getString(KEY_THEME, ThemeMode.SYSTEM.name)!!)
        }.getOrDefault(ThemeMode.SYSTEM)
    )

    private fun write(next: UserSettings) {
        prefs.edit()
            .putBoolean(KEY_AUTO, next.autoLoginEnabled)
            .putBoolean(KEY_DYNAMIC, next.useDynamicColor)
            .putString(KEY_PALETTE, next.palette.name)
            .putString(KEY_THEME, next.themeMode.name)
            .apply()
        _state.value = next
    }

    fun setAutoLoginEnabled(enabled: Boolean) {
        write(_state.value.copy(autoLoginEnabled = enabled))
    }

    fun setUseDynamicColor(enabled: Boolean) {
        write(_state.value.copy(useDynamicColor = enabled))
    }

    fun setPalette(palette: ColorPalette) {
        write(_state.value.copy(useDynamicColor = false, palette = palette))
    }

    fun setThemeMode(mode: ThemeMode) {
        write(_state.value.copy(themeMode = mode))
    }

    companion object {
        private const val PREFS = "connectme_settings"
        private const val KEY_AUTO = "auto_login"
        private const val KEY_DYNAMIC = "dynamic_color"
        private const val KEY_PALETTE = "palette"
        private const val KEY_THEME = "theme_mode"
    }
}
