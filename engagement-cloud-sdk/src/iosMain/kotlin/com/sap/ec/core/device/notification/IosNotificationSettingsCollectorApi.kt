package com.sap.ec.core.device.notification

interface IosNotificationSettingsCollectorApi {

    suspend fun collect(): IosNotificationSettings

}