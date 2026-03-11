package com.sap.ec.core.permission

import kotlinx.coroutines.await
import org.w3c.notifications.Notification

internal class WebPermissionHandler : PermissionHandlerApi {

    override suspend fun requestPushPermission() {
        if (js("'Notification' in window")) {
            Notification.requestPermission().await()
        }
    }
}