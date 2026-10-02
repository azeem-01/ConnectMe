package com.ce46.connectme.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ce46.connectme.ConnectMeApp
import com.ce46.connectme.data.ColorPalette
import com.ce46.connectme.data.ThemeMode
import com.ce46.connectme.data.UpdateChecker
import com.ce46.connectme.data.UpdateInfo
import com.ce46.connectme.service.CaptivePortalService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val hasCredentials: Boolean = false,
    val maskedStudentId: String = "",
    val autoLoginEnabled: Boolean = false,
    val useDynamicColor: Boolean = true,
    val palette: ColorPalette = ColorPalette.MINT,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val checkingUpdate: Boolean = false,
    val updateInfo: UpdateInfo? = null,
    val message: String? = null
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val container = ConnectMeApp.instance.container
    private val updateChecker = UpdateChecker()

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        refresh()
        viewModelScope.launch {
            container.settings.state.collect { settings ->
                _uiState.update { current ->
                    current.copy(
                        autoLoginEnabled = settings.autoLoginEnabled,
                        useDynamicColor = settings.useDynamicColor,
                        palette = settings.palette,
                        themeMode = settings.themeMode
                    )
                }
            }
        }
    }

    fun refresh() {
        val hasCreds = container.credentials.hasCredentials()
        val masked = if (hasCreds) container.credentials.studentIdMasked() else ""
        val settings = container.settings.state.value
        _uiState.update {
            it.copy(
                hasCredentials = hasCreds,
                maskedStudentId = masked,
                autoLoginEnabled = settings.autoLoginEnabled,
                useDynamicColor = settings.useDynamicColor,
                palette = settings.palette,
                themeMode = settings.themeMode
            )
        }
    }

    fun saveCredentials(studentId: String, password: String) {
        if (studentId.isBlank() || password.isBlank()) return
        container.credentials.save(studentId.trim(), password.trim())
        // Auto-enable auto-login if saving for the first time
        container.settings.setAutoLoginEnabled(true)
        CaptivePortalService.sync(getApplication(), true)
        container.status.append("Credentials updated & secured")
        _uiState.update {
            it.copy(
                hasCredentials = true,
                maskedStudentId = container.credentials.studentIdMasked(),
                message = "Credentials saved securely! ✅"
            )
        }
    }

    fun clearCredentials() {
        container.credentials.clear()
        container.settings.setAutoLoginEnabled(false)
        CaptivePortalService.sync(getApplication(), false)
        container.status.append("Credentials removed")
        _uiState.update {
            it.copy(
                hasCredentials = false,
                maskedStudentId = "",
                message = "Credentials deleted from Keystore"
            )
        }
    }

    fun setAutoLogin(enabled: Boolean) {
        if (enabled && !container.credentials.hasCredentials()) {
            _uiState.update { it.copy(message = "Please save your credentials first") }
            return
        }
        container.settings.setAutoLoginEnabled(enabled)
        CaptivePortalService.sync(getApplication(), enabled)
    }

    fun setDynamicColor(enabled: Boolean) {
        container.settings.setUseDynamicColor(enabled)
    }

    fun setPalette(palette: ColorPalette) {
        container.settings.setPalette(palette)
    }

    fun setThemeMode(mode: ThemeMode) {
        container.settings.setThemeMode(mode)
    }

    fun clearActivityLog() {
        container.activityLog.clear()
        container.status.restore()
        _uiState.update { it.copy(message = "Activity log cleared") }
    }

    fun checkForUpdates() {
        viewModelScope.launch {
            _uiState.update { it.copy(checkingUpdate = true) }
            val info = updateChecker.check()
            _uiState.update { it.copy(checkingUpdate = false, updateInfo = info) }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}
