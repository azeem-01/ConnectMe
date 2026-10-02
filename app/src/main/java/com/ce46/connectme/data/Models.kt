package com.ce46.connectme.data

enum class ConnectionState {
    IDLE,
    WATCHING,
    PORTAL_DETECTED,
    LOGGING_IN,
    LOGGED_IN,
    ERROR
}

enum class ThemeMode { SYSTEM, LIGHT, DARK }

enum class ColorPalette(val label: String) {
    MINT("Mint"),
    SKY("Sky"),
    CORAL("Coral"),
    GRAPE("Grape"),
    SUNSET("Sunset")
}

data class Credentials(val studentId: String, val password: String)

data class LogLine(val time: String, val message: String)

data class WifiAccessPoint(
    val ssid: String,
    val bssid: String,
    val level: Int,
    val frequency: Int,
    val capabilities: String,
    val isCurrent: Boolean
) {
    val isOpen: Boolean
        get() {
            val cap = capabilities.uppercase()
            return listOf("WEP", "WPA", "RSN", "PSK", "EAP", "SAE").none { cap.contains(it) }
        }

    val securityLabel: String
        get() = when {
            isOpen -> "Open · portal possible"
            capabilities.uppercase().contains("EAP") -> "WPA-Enterprise"
            else -> "Secured"
        }
}

data class UserSettings(
    val autoLoginEnabled: Boolean = false,
    val useDynamicColor: Boolean = true,
    val palette: ColorPalette = ColorPalette.MINT,
    val themeMode: ThemeMode = ThemeMode.SYSTEM
)
