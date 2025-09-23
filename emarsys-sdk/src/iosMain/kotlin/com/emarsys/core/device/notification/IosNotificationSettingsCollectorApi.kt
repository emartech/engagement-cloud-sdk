package com.emarsys.core.device.notification

interface IosNotificationSettingsCollectorApi {

    suspend fun collect(): IosNotificationSettings

}