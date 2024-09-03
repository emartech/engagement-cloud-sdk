package com.emarsys.core.permission

import kotlinx.coroutines.await
import org.w3c.notifications.Notification

class WebPermissionHandler : PermissionHandlerApi {

    override suspend fun requestPushPermission() {
        Notification.requestPermission().await()
    }
}