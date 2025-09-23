package com.emarsys.core.device.notification

import com.emarsys.mobileengage.push.PushServiceContextApi

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