package com.ce46.connectme.di

import android.content.Context
import com.ce46.connectme.data.ActivityLogStore
import com.ce46.connectme.data.AppStatus
import com.ce46.connectme.data.CredentialStore
import com.ce46.connectme.data.SettingsStore
import com.ce46.connectme.wifi.WifiRepository

class AppContainer(context: Context) {
    val appContext: Context = context.applicationContext
    val credentials = CredentialStore(appContext)
    val settings = SettingsStore(appContext)
    val activityLog = ActivityLogStore(appContext)
    val status = AppStatus(activityLog)
    val wifi = WifiRepository(appContext)
}
