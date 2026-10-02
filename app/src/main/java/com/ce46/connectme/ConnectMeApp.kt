package com.ce46.connectme

import android.app.Application
import com.ce46.connectme.di.AppContainer

class ConnectMeApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        container = AppContainer(this)
        container.status.restore()
    }

    companion object {
        lateinit var instance: ConnectMeApp
            private set
    }
}
