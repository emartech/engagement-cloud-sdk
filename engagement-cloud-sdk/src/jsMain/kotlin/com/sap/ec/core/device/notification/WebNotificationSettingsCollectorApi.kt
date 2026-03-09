package com.sap.ec.core.device.notification

internal interface WebNotificationSettingsCollectorApi {

    suspend fun collect(): WebNotificationSettings

}