package com.sap.ec.core.watchdog.connection

import com.sap.ec.watchdog.connection.ConnectionWatchDog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class IosConnectionWatchdog(
    private val reachabilityWrapper: Reachability
) : ConnectionWatchDog {

    private val _isOnline = MutableStateFlow(true)
    override val isOnline = _isOnline.asStateFlow()

    override suspend fun register() {
        _isOnline.value = reachabilityWrapper.isConnected()
        reachabilityWrapper.subscribeToNetworkChanges { isOnline ->
            _isOnline.value = isOnline
        }
    }
}