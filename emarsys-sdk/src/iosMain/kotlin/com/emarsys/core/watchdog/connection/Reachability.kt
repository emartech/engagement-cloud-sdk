package com.emarsys.core.watchdog.connection

interface Reachability {
    fun subscribeToNetworkChanges(lambda: (Boolean) -> Unit)
    fun isConnected(): Boolean
    fun getNetworkConnection(): NetworkConnection
}