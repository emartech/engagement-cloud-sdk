package com.sap.ec.core.device.notification

interface WebNotificationSettingsCollectorApi {

    suspend fun collect(): WebNotificationSettings

}