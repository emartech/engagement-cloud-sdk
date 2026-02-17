package com.sap.ec.watchdog.connection

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.w3c.dom.Window
import org.w3c.dom.events.Event


class WebConnectionWatchDog(
    private val window: Window
) :
    ConnectionWatchDog {

    private val _isOnline = MutableStateFlow(window.navigator.onLine)

    override val isOnline = _isOnline.asStateFlow()

    override suspend fun register() {
        window.addEventListener("online", this::onOnline)
        window.addEventListener("offline", this::onOffline)
    }

    private fun onOnline(event: Event) {
        _isOnline.value = true
    }

    private fun onOffline(event: Event) {
        _isOnline.value = false
    }
}