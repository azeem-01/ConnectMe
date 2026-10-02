package com.ce46.connectme.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ce46.connectme.ConnectMeApp

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        val container = ConnectMeApp.instance.container
        if (container.settings.state.value.autoLoginEnabled && container.credentials.hasCredentials()) {
            CaptivePortalService.start(context)
        }
    }
}
