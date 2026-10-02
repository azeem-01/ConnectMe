package com.ce46.connectme.wifi

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.ce46.connectme.data.WifiAccessPoint
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class WifiRepository(private val context: Context) {

    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val connectivityManager =
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val isWifiEnabled: Boolean get() = wifiManager.isWifiEnabled

    fun wifiEnabledFlow(): Flow<Boolean> = callbackFlow {
        trySend(wifiManager.isWifiEnabled)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                trySend(wifiManager.isWifiEnabled)
            }
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        awaitClose { runCatching { context.unregisterReceiver(receiver) } }
    }.distinctUntilChanged()

    fun scanResults(): Flow<List<WifiAccessPoint>> = callbackFlow {
        fun emit() {
            trySend(mapResults())
        }
        emit()
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                emit()
            }
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        awaitClose { runCatching { context.unregisterReceiver(receiver) } }
    }

    @SuppressLint("MissingPermission")
    fun startScan(): Boolean = runCatching { wifiManager.startScan() }.getOrDefault(false)

    @SuppressLint("MissingPermission")
    fun currentSsid(): String? {
        val info = wifiInfo() ?: return null
        val raw = info.ssid ?: return null
        if (raw == WifiManager.UNKNOWN_SSID || raw == "<unknown ssid>") return null
        return raw.trim('"').takeIf { it.isNotBlank() }
    }

    fun openWifiPanel() {
        context.startActivity(
            Intent(Settings.Panel.ACTION_WIFI).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    fun openLocationSettings() {
        context.startActivity(
            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    fun addOpenNetworkSuggestion(ssid: String) {
        val suggestion = WifiNetworkSuggestion.Builder()
            .setSsid(ssid)
            .setIsAppInteractionRequired(true)
            .build()
        runCatching { wifiManager.addNetworkSuggestions(listOf(suggestion)) }
    }

    fun promptSaveNetwork(ssid: String, isOpen: Boolean) {
        val builder = WifiNetworkSuggestion.Builder().setSsid(ssid)
        if (isOpen) {
            builder.setIsAppInteractionRequired(true)
        }
        val suggestion = builder.build()
        val intent = Intent(Settings.ACTION_WIFI_ADD_NETWORKS).apply {
            putParcelableArrayListExtra(
                Settings.EXTRA_WIFI_NETWORK_LIST,
                arrayListOf(suggestion)
            )
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        if (isOpen) addOpenNetworkSuggestion(ssid)
    }

    @SuppressLint("MissingPermission")
    fun getCachedScanResults(): List<WifiAccessPoint> = mapResults()

    @SuppressLint("MissingPermission")
    private fun mapResults(): List<WifiAccessPoint> {
        val current = currentSsid()
        val results: List<ScanResult> = runCatching { wifiManager.scanResults }.getOrDefault(emptyList())
        val mapped = results
            .mapNotNull { scan ->
                val raw = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    scan.wifiSsid?.toString()?.takeIf { it.isNotBlank() } ?: @Suppress("DEPRECATION") scan.SSID
                } else {
                    @Suppress("DEPRECATION") scan.SSID
                }?.trim()?.trim('"').orEmpty()
                if (raw.isBlank() || raw == "<unknown ssid>" || raw == WifiManager.UNKNOWN_SSID) return@mapNotNull null
                WifiAccessPoint(
                    ssid = raw,
                    bssid = scan.BSSID ?: "",
                    level = scan.level,
                    frequency = scan.frequency,
                    capabilities = scan.capabilities ?: "",
                    isCurrent = raw == current
                )
            }
            .groupBy { it.ssid }
            .map { (ssid, group) ->
                group.maxBy { it.level }.copy(isCurrent = group.any { it.isCurrent } || ssid == current)
            }
            .toMutableList()

        if (current != null && mapped.none { it.ssid == current }) {
            mapped.add(
                0,
                WifiAccessPoint(
                    ssid = current,
                    bssid = "",
                    level = -50,
                    frequency = 2412,
                    capabilities = "[ESS]",
                    isCurrent = true
                )
            )
        }

        return mapped.sortedWith(
            compareByDescending<WifiAccessPoint> { it.isCurrent }.thenByDescending { it.level }
        )
    }

    private fun wifiInfo(): WifiInfo? {
        val network: Network = connectivityManager.activeNetwork ?: return null
        val caps = connectivityManager.getNetworkCapabilities(network) ?: return null
        if (!caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return null
        return caps.transportInfo as? WifiInfo
    }
}
