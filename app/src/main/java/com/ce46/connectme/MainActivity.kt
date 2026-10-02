package com.ce46.connectme

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.ce46.connectme.ui.nav.ConnectTab
import com.ce46.connectme.ui.nav.SlidingBottomBar
import com.ce46.connectme.ui.settings.SettingsScreen
import com.ce46.connectme.ui.status.StatusScreen
import com.ce46.connectme.ui.theme.ConnectMeTheme
import com.ce46.connectme.ui.wifi.WifiScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val container = ConnectMeApp.instance.container
            val settings by container.settings.state.collectAsState()

            ConnectMeTheme(
                themeMode = settings.themeMode,
                useDynamicColor = settings.useDynamicColor,
                palette = settings.palette
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ConnectMeMainScaffold()
                }
            }
        }
    }
}

@Composable
fun ConnectMeMainScaffold() {
    var currentTab by rememberSaveable { mutableStateOf(ConnectTab.WIFI) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            SlidingBottomBar(
                selected = currentTab,
                onSelect = { currentTab = it }
            )
        }
    ) { paddingValues ->
        AnimatedContent(
            targetState = currentTab,
            transitionSpec = {
                fadeIn(animationSpec = tween(220)) togetherWith
                    fadeOut(animationSpec = tween(160))
            },
            label = "TabContentAnimation"
        ) { tab ->
            when (tab) {
                ConnectTab.WIFI -> WifiScreen(
                    padding = paddingValues
                )
                ConnectTab.STATUS -> StatusScreen(
                    padding = paddingValues,
                    onNavigateToSettings = { currentTab = ConnectTab.SETTINGS }
                )
                ConnectTab.SETTINGS -> SettingsScreen(
                    padding = paddingValues,
                    onShowSnackbar = { message ->
                        scope.launch {
                            snackbarHostState.showSnackbar(message)
                        }
                    }
                )
            }
        }
    }
}
