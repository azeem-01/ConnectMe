package com.ce46.connectme.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.ce46.connectme.ConnectMeApp
import com.ce46.connectme.MainActivity
import com.ce46.connectme.R
import com.ce46.connectme.data.ConnectionState
import com.ce46.connectme.data.PortalLoginClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CaptivePortalService : Service() {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private lateinit var connectivityManager: ConnectivityManager

    private var wifiNetwork: Network? = null
    private var confirmedOnlineForNetwork: Network? = null
    private var pollingJob: Job? = null
    private var isCallbackRegistered = false

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            val caps = connectivityManager.getNetworkCapabilities(network) ?: return
            if (!caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return
            if (wifiNetwork != network) {
                wifiNetwork = network
                confirmedOnlineForNetwork = null
                status().append("Wi-Fi available, checking portal")
                startPolling(network)
            }
        }

        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            if (!capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return
            if (wifiNetwork != network) {
                wifiNetwork = network
                confirmedOnlineForNetwork = null
                status().append("Wi-Fi changed, checking it directly")
                startPolling(network)
            }
        }

        override fun onLost(network: Network) {
            if (network == wifiNetwork) {
                status().append("Wi-Fi lost — watching again")
                wifiNetwork = null
                confirmedOnlineForNetwork = null
                pollingJob?.cancel()
                status().currentSsid(null)
                status().update(ConnectionState.WATCHING)
                updateNotification("Watching for campus Wi-Fi…")
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification("Watching for campus Wi-Fi…"))
        status().update(ConnectionState.WATCHING)
        status().append("Auto-login is on")

        if (!isCallbackRegistered) {
            val request = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build()
            connectivityManager.requestNetwork(request, networkCallback)
            isCallbackRegistered = true
        }
        return START_STICKY
    }

    private fun startPolling(network: Network) {
        pollingJob?.cancel()
        pollingJob = scope.launch {
            while (wifiNetwork == network && confirmedOnlineForNetwork != network) {
                checkNetwork(network)
                delay(10_000)
            }
        }
    }

    private suspend fun checkNetwork(network: Network) {
        val creds = ConnectMeApp.instance.container.credentials.load()
        if (creds == null) {
            status().append("Wi-Fi up, but no saved credentials")
            return
        }
        val ssid = ConnectMeApp.instance.container.wifi.currentSsid()
        status().currentSsid(ssid)
        status().update(ConnectionState.LOGGING_IN)
        val client = PortalLoginClient(network)
        connectivityManager.bindProcessToNetwork(network)
        try {
            when (val result = client.attemptLogin(creds.studentId, creds.password)) {
                is PortalLoginClient.Result.LoggedIn -> {
                    confirmedOnlineForNetwork = network
                    status().update(ConnectionState.LOGGED_IN)
                    status().append(result.message)
                    updateNotification("Signed in${ssid?.let { " · $it" } ?: ""}")
                }
                is PortalLoginClient.Result.AlreadyOnline -> {
                    confirmedOnlineForNetwork = network
                    status().update(ConnectionState.WATCHING)
                    status().append(result.message)
                    updateNotification("Watching for campus Wi-Fi…")
                }
                is PortalLoginClient.Result.NoPortalYet -> {
                    status().update(ConnectionState.WATCHING)
                    status().append(result.message)
                }
                is PortalLoginClient.Result.Failure -> {
                    status().update(ConnectionState.ERROR)
                    status().append("Retrying: ${result.error}")
                    updateNotification("Retrying login…")
                }
            }
        } finally {
            connectivityManager.bindProcessToNetwork(null)
        }
    }

    private fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel),
            NotificationManager.IMPORTANCE_MIN
        ).apply {
            setShowBadge(false)
            enableLights(false)
            enableVibration(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(text: String): Notification {
        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ConnectMe")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_qs_tile)
            .setContentIntent(open)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
    }

    private fun updateNotification(text: String) {
        // No-op: Notifications disabled per user preference
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isCallbackRegistered) {
            runCatching { connectivityManager.unregisterNetworkCallback(networkCallback) }
            isCallbackRegistered = false
        }
        connectivityManager.bindProcessToNetwork(null)
        pollingJob?.cancel()
        scope.cancel()
        status().update(ConnectionState.IDLE)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun status() = ConnectMeApp.instance.container.status

    companion object {
        private const val CHANNEL_ID = "connectme_portal"
        private const val NOTIFICATION_ID = 46

        fun start(context: Context) {
            context.startForegroundService(Intent(context, CaptivePortalService::class.java))
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, CaptivePortalService::class.java))
        }

        fun sync(context: Context, enabled: Boolean) {
            if (enabled) start(context) else stop(context)
            AutoLoginTileService.requestRefresh(context)
        }
    }
}
