package com.emarsys.core.watchdog.connection

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import platform.Network.nw_interface_type_cellular
import platform.Network.nw_interface_type_loopback
import platform.Network.nw_interface_type_other
import platform.Network.nw_interface_type_wifi
import platform.Network.nw_interface_type_wired
import platform.Network.nw_path_get_status
import platform.Network.nw_path_monitor_cancel
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_monitor_t
import platform.Network.nw_path_status_satisfied
import platform.Network.nw_path_uses_interface_type
import platform.darwin.dispatch_queue_create
import platform.darwin.dispatch_queue_t

class NWPathMonitorWrapper(
    private val sdkDispatcher: CoroutineDispatcher
) : Reachability {
    private val pathMonitor: nw_path_monitor_t = nw_path_monitor_create()
    private var connectionStatus: NetworkConnection = NetworkConnection.None
    private var isSatisfied: Boolean = true

    init {
        val dispatchQueue: dispatch_queue_t = dispatch_queue_create("ems_queue", null)
        nw_path_monitor_set_queue(pathMonitor, dispatchQueue)
    }

    override fun subscribeToNetworkChanges(lambda: (Boolean) -> Unit) {
        nw_path_monitor_set_update_handler(pathMonitor) { path ->
            CoroutineScope(sdkDispatcher).launch {
                val status = nw_path_get_status(path)
                isSatisfied = status == nw_path_status_satisfied

                connectionStatus = NetworkConnection.None
                if (isSatisfied) {
                    when {
                        nw_path_uses_interface_type(path, nw_interface_type_wifi) -> {
                            connectionStatus = NetworkConnection.Wifi
                        }

                        nw_path_uses_interface_type(path, nw_interface_type_cellular) -> {
                            connectionStatus = NetworkConnection.Cellular
                        }

                        nw_path_uses_interface_type(path, nw_interface_type_wired) -> {
                            connectionStatus = NetworkConnection.Wired
                        }

                        nw_path_uses_interface_type(path, nw_interface_type_loopback) -> {
                            connectionStatus = NetworkConnection.Loopback
                        }

                        nw_path_uses_interface_type(path, nw_interface_type_other) -> {
                            connectionStatus = NetworkConnection.Other
                        }
                    }
                }
                lambda(isSatisfied)
            }
        }
        nw_path_monitor_start(pathMonitor)
    }

    override fun isConnected(): Boolean {
        return isSatisfied
    }

    override fun getNetworkConnection(): NetworkConnection {
        return connectionStatus
    }

    fun stopObserving() {
        nw_path_monitor_cancel(pathMonitor)
    }

}