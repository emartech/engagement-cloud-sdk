package com.emarsys.core.device.notification

interface WebNotificationSettingsCollectorApi {

    suspend fun collect(): WebNotificationSettings

}