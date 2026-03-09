package com.sap.ec.core.permission

import kotlinx.coroutines.await
import org.w3c.notifications.Notification

internal class WebPermissionHandler : PermissionHandlerApi {

    override suspend fun requestPushPermission() {
        Notification.requestPermission().await()
    }
}