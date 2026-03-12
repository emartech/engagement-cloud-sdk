package com.sap.ec.core.device.notification

import com.sap.ec.mobileengage.push.JsPushWrapperApi
import com.sap.ec.mobileengage.push.PushServiceApi

internal class WebNotificationSettingsCollector(
    private val pushService: PushServiceApi,
    private val jsPushWrapperApi: JsPushWrapperApi
) :
    WebNotificationSettingsCollectorApi {

    override suspend fun collect(): WebNotificationSettings {
        return WebNotificationSettings(
            permissionState = pushService.getPermissionState(),
            isServiceWorkerRegistered = pushService.getServiceWorkerRegistration()?.active != null,
            isSubscribed = jsPushWrapperApi.isSubscribed()
        )
    }
}