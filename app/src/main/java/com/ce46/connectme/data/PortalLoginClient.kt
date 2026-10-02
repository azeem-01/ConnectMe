package com.ce46.connectme.data

import android.net.Network
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Dns
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.InetAddress
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

class PortalLoginClient(network: Network) {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .socketFactory(network.socketFactory)
        .dns(NetworkBoundDns(network))
        .followRedirects(true)
        .followSslRedirects(true)
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()

    private class NetworkBoundDns(private val network: Network) : Dns {
        override fun lookup(hostname: String): List<InetAddress> {
            return network.getAllByName(hostname).toList()
        }
    }

    sealed class Result {
        data class AlreadyOnline(val message: String) : Result()
        data class LoggedIn(val message: String) : Result()
        data class NoPortalYet(val message: String) : Result()
        data class Failure(val error: String) : Result()
    }

    suspend fun attemptLogin(username: String, password: String): Result =
        withContext(Dispatchers.IO) {
            try {
                val canaryRequest = Request.Builder()
                    .url("http://connectivitycheck.gstatic.com/generate_204")
                    .header(
                        "User-Agent",
                        "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 " +
                            "(KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
                    )
                    .build()

                client.newCall(canaryRequest).execute().use { response ->
                    val finalUrl = response.request.url.toString()
                    when {
                        finalUrl.contains("fgtauth") -> {
                            val magicToken = finalUrl.substringAfterLast('?')
                            val basePostUrl = finalUrl.substringBefore("fgtauth")
                            val formBody = FormBody.Builder()
                                .add("username", username)
                                .add("password", password)
                                .add("magic", magicToken)
                                .build()
                            val loginRequest = Request.Builder()
                                .url(basePostUrl)
                                .post(formBody)
                                .build()
                            client.newCall(loginRequest).execute().use { loginResponse ->
                                if (loginResponse.isSuccessful) {
                                    Result.LoggedIn("Portal accepted credentials (HTTP ${loginResponse.code})")
                                } else {
                                    Result.Failure("Portal rejected login (HTTP ${loginResponse.code})")
                                }
                            }
                        }
                        response.code == 204 -> Result.AlreadyOnline("Already online (HTTP 204)")
                        finalUrl.contains("connectivitycheck") ->
                            Result.AlreadyOnline("Already online, no portal")
                        else -> Result.NoPortalYet("Unexpected redirect: $finalUrl")
                    }
                }
            } catch (_: UnknownHostException) {
                Result.Failure("DNS failed on this Wi-Fi")
            } catch (_: SocketTimeoutException) {
                Result.Failure("Timed out — network still settling")
            } catch (e: Exception) {
                Result.Failure(e.message ?: "Unknown network error")
            }
        }
}
