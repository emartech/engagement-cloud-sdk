package com.emarsys.watchdog.connection

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.emarsys.core.log.LogEntry
import com.emarsys.core.log.LogLevel
import com.emarsys.core.log.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AndroidConnectionWatchDog(
    private val connectivityManager: ConnectivityManager,
    private val logger: Logger
) :
    ConnectionWatchDog, ConnectivityManager.NetworkCallback() {

    private val _isOnline = MutableStateFlow(isConnected())

    override val isOnline = _isOnline.asStateFlow()
    override suspend fun register() {
        logger.log(LogEntry("log_network_monitoring_start", mapOf()), LogLevel.Debug)
        connectivityManager.registerDefaultNetworkCallback(this)
    }

    override fun onAvailable(network: Network) {
        logger.log(LogEntry("log_network_connection_available", mapOf()), LogLevel.Debug)
        _isOnline.value = true
    }

    override fun onLost(network: Network) {
        logger.log(LogEntry("log_network_connection_NOT_available", mapOf()), LogLevel.Debug)
        _isOnline.value = false
    }

    private fun isConnected() = try {
        connectivityManager.activeNetwork?.let {
            connectivityManager.getNetworkCapabilities(it)
                ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } ?: false
    } catch (ignored: Exception) {
        logger.log(
            LogEntry(
                "log_error_getting_network_connection",
                mapOf("message" to (ignored.message ?: "Error getting network connection"))
            ), LogLevel.Debug
        )
        false
    }

}