package com.ce46.connectme.ui.wifi

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ce46.connectme.ConnectMeApp
import com.ce46.connectme.service.CaptivePortalService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WifiUiState(
    val wifiEnabled: Boolean = true,
    val currentSsid: String? = null,
    val message: String? = null
)

class WifiViewModel(application: Application) : AndroidViewModel(application) {
    private val container = ConnectMeApp.instance.container
    private val wifi = container.wifi
    private val _state = MutableStateFlow(WifiUiState())
    val state: StateFlow<WifiUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            wifi.wifiEnabledFlow().collect { enabled ->
                _state.update { it.copy(wifiEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            container.status.ssid.collect { ssid ->
                _state.update { it.copy(currentSsid = ssid ?: wifi.currentSsid()) }
            }
        }
    }

    fun refreshCurrentNetwork() {
        val ssid = container.status.ssid.value ?: wifi.currentSsid()
        _state.update { it.copy(currentSsid = ssid) }
    }

    fun triggerPortalSignIn() {
        val current = _state.value.currentSsid
        if (container.credentials.hasCredentials()) {
            container.settings.setAutoLoginEnabled(true)
            CaptivePortalService.sync(getApplication(), true)
            container.status.append("Signing into portal${current?.let { " on $it" } ?: ""}")
            _state.update { it.copy(message = "Signing into portal…") }
        } else {
            _state.update { it.copy(message = "Set up credentials in Settings first") }
        }
    }

    fun openWifiPanel() = wifi.openWifiPanel()
}
