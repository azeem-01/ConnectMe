package com.ce46.connectme.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ce46.connectme.ConnectMeApp

/** Fires after a suggested network connects — kick the portal watcher. */
class SuggestionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val container = ConnectMeApp.instance.container
        if (container.settings.state.value.autoLoginEnabled && container.credentials.hasCredentials()) {
            container.status.append("Suggested network connected")
            CaptivePortalService.start(context)
        }
    }
}
