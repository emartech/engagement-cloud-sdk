package com.emarsys.watchdog.connection

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.emarsys.core.log.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

class AndroidConnectionWatchDog(
    private val connectivityManager: ConnectivityManager,
    private val logger: Logger
) :
    ConnectionWatchDog, ConnectivityManager.NetworkCallback() {

    private val _isOnline = MutableStateFlow(isConnected())

    override val isOnline = _isOnline.asStateFlow()
    override suspend fun register() {
        logger.debug("log_network_monitoring_start")
        connectivityManager.registerDefaultNetworkCallback(this)
    }

    override fun onAvailable(network: Network) {
        CoroutineScope(Dispatchers.IO).launch {
            logger.debug("log_network_connection_available")
        }
        _isOnline.value = true
    }

    override fun onLost(network: Network) {
        CoroutineScope(Dispatchers.IO).launch {
            logger.debug("log_network_connection_NOT_available")
        }
        _isOnline.value = false
    }

    private fun isConnected() = try {
        connectivityManager.activeNetwork?.let {
            connectivityManager.getNetworkCapabilities(it)
                ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } ?: false
    } catch (ignored: Exception) {
        CoroutineScope(Dispatchers.IO).launch {
            logger.debug(
                "log_error_getting_network_connection",
                buildJsonObject {
                    put(
                        "message",
                        JsonPrimitive(ignored.message ?: "Error getting network connection")
                    )
                }
            )
        }
        false
    }

}