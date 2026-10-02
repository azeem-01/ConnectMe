package com.ce46.connectme.data

import com.ce46.connectme.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class UpdateInfo(
    val currentName: String,
    val currentCode: Int,
    val latestName: String?,
    val latestCode: Int?,
    val apkUrl: String?,
    val statusLabel: String,
    val hasUpdate: Boolean
)

class UpdateChecker {
    /**
     * Optional JSON: {"versionCode":2,"versionName":"1.1.0","apkUrl":"https://..."}
     * Leave empty until you host a release manifest.
     */
    private val manifestUrl: String = ""

    suspend fun check(): UpdateInfo = withContext(Dispatchers.IO) {
        val currentName = BuildConfig.VERSION_NAME
        val currentCode = BuildConfig.VERSION_CODE
        if (manifestUrl.isBlank()) {
            return@withContext UpdateInfo(
                currentName = currentName,
                currentCode = currentCode,
                latestName = null,
                latestCode = null,
                apkUrl = null,
                statusLabel = "v$currentName · up to date on this phone",
                hasUpdate = false
            )
        }
        try {
            val client = OkHttpClient.Builder()
                .callTimeout(8, TimeUnit.SECONDS)
                .build()
            val body = client.newCall(Request.Builder().url(manifestUrl).build()).execute().use {
                it.body?.string().orEmpty()
            }
            val json = JSONObject(body)
            val latestCode = json.optInt("versionCode", currentCode)
            val latestName = json.optString("versionName", currentName)
            val apkUrl = json.optString("apkUrl").ifBlank { null }
            val hasUpdate = latestCode > currentCode
            UpdateInfo(
                currentName, currentCode, latestName, latestCode, apkUrl,
                statusLabel = if (hasUpdate) "Update available · v$latestName" else "You're on the latest · v$currentName",
                hasUpdate = hasUpdate
            )
        } catch (_: Exception) {
            UpdateInfo(
                currentName, currentCode, null, null, null,
                statusLabel = "Couldn't reach updates · v$currentName",
                hasUpdate = false
            )
        }
    }
}
