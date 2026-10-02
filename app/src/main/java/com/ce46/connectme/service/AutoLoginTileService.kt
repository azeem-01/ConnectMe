package com.ce46.connectme.service

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.ce46.connectme.ConnectMeApp
import com.ce46.connectme.MainActivity
import com.ce46.connectme.data.ConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Quick Settings tile provides one-tap campus Wi-Fi auto-login toggle
 * and quick portal trigger directly from the notification shade.
 */
class AutoLoginTileService : TileService() {

    private var listeningScope: CoroutineScope? = null

    override fun onStartListening() {
        super.onStartListening()
        refresh()
        val scope = CoroutineScope(Dispatchers.Main + Job())
        listeningScope = scope
        scope.launch {
            ConnectMeApp.instance.container.status.state.collect { refresh() }
        }
        scope.launch {
            ConnectMeApp.instance.container.status.ssid.collect { refresh() }
        }
        scope.launch {
            ConnectMeApp.instance.container.settings.state.collect { refresh() }
        }
    }

    override fun onStopListening() {
        super.onStopListening()
        listeningScope?.cancel()
        listeningScope = null
    }

    override fun onClick() {
        super.onClick()
        val container = ConnectMeApp.instance.container
        if (!container.credentials.hasCredentials()) {
            openApp()
            return
        }
        val enabled = container.settings.state.value.autoLoginEnabled
        val nextState = !enabled
        container.settings.setAutoLoginEnabled(nextState)
        CaptivePortalService.sync(this, nextState)
        refresh()
    }

    private fun openApp() {
        val intent = Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startActivityAndCollapse(
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            )
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }

    private fun refresh() {
        val tile = qsTile ?: return
        val container = runCatching { ConnectMeApp.instance.container }.getOrNull() ?: return
        val hasCreds = container.credentials.hasCredentials()
        val auto = container.settings.state.value.autoLoginEnabled
        val connection = container.status.state.value
        val ssid = container.status.ssid.value

        tile.state = when {
            !hasCreds -> Tile.STATE_UNAVAILABLE
            auto -> Tile.STATE_ACTIVE
            else -> Tile.STATE_INACTIVE
        }
        tile.label = "ConnectMe"
        tile.subtitle = when {
            !hasCreds -> "Set up Student ID"
            !auto -> "Off"
            connection == ConnectionState.LOGGED_IN -> ssid ?: "Signed in ✅"
            connection == ConnectionState.LOGGING_IN -> "Signing in…"
            connection == ConnectionState.WATCHING -> "Watching"
            connection == ConnectionState.ERROR -> "Retrying…"
            else -> "Active"
        }
        tile.updateTile()
    }

    companion object {
        fun requestRefresh(context: Context) {
            requestListeningState(
                context,
                ComponentName(context, AutoLoginTileService::class.java)
            )
        }
    }
}
