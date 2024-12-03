 package com.emarsys.notification

import com.emarsys.self
import com.emarsys.window.BrowserWindowHandlerApi
import web.broadcast.BroadcastChannel
import web.serviceworker.WindowClient

class NotificationClickHandler(
    private val onNotificationClickedBroadcastChannel: BroadcastChannel,
    private val browserWindowHandler: BrowserWindowHandlerApi
) {

    internal var storedNotificationClickedMessage: String? = null

    suspend fun handleNotificationClick(jsNotificationClickedData: String) {
        val openWindow = browserWindowHandler.findOpenWindow()
        if (openWindow != null) {
            postMessageToOpenWindow(
                openWindow,
                jsNotificationClickedData
            )
        } else {
            storeMessageAndOpenWindow(jsNotificationClickedData)
        }
    }

    fun postStoredMessageToSDK() {
        storedNotificationClickedMessage?.let {
            onNotificationClickedBroadcastChannel.postMessage(it)
            storedNotificationClickedMessage = null
        }
    }

    private suspend fun postMessageToOpenWindow(
        openWindow: WindowClient,
        jsNotificationClickedData: String
    ) {
        openWindow.focus()
        onNotificationClickedBroadcastChannel.postMessage(
            jsNotificationClickedData
        )
    }

    private suspend fun storeMessageAndOpenWindow(
        jsNotificationClickedData: String
    ): WindowClient? {
        storedNotificationClickedMessage = jsNotificationClickedData
        val windowClient = browserWindowHandler.openWindow(self.location.origin)
        return windowClient?.focus()
    }
}