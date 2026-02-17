package com.sap.ec.core.device.notification

import com.sap.ec.mobileengage.push.PushServiceContextApi

class WebNotificationSettingsCollector(private val pushServiceContext: PushServiceContextApi) :
    WebNotificationSettingsCollectorApi {

    override suspend fun collect(): WebNotificationSettings {
        return WebNotificationSettings(
            permissionState = pushServiceContext.getPermissionState(),
            isServiceWorkerRegistered = pushServiceContext.isServiceWorkerRegistered,
            isSubscribed = pushServiceContext.isSubscribed
        )
    }
}