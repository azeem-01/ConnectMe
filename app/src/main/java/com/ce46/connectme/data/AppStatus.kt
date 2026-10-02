package com.ce46.connectme.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppStatus(private val logStore: ActivityLogStore) {
    private val _state = MutableStateFlow(ConnectionState.IDLE)
    val state: StateFlow<ConnectionState> = _state.asStateFlow()

    private val _log = MutableStateFlow<List<LogLine>>(emptyList())
    val log: StateFlow<List<LogLine>> = _log.asStateFlow()

    private val _ssid = MutableStateFlow<String?>(null)
    val ssid: StateFlow<String?> = _ssid.asStateFlow()

    fun restore() {
        _log.value = logStore.restore()
    }

    fun update(state: ConnectionState) {
        _state.value = state
    }

    fun currentSsid(ssid: String?) {
        _ssid.value = ssid
    }

    fun append(message: String) {
        _log.value = logStore.append(message)
    }
}
