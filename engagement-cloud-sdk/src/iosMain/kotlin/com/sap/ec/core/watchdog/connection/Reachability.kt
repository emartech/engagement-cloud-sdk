package com.sap.ec.core.watchdog.connection

internal interface Reachability {
    fun subscribeToNetworkChanges(lambda: (Boolean) -> Unit)
    fun isConnected(): Boolean
    fun getNetworkConnection(): NetworkConnection
}