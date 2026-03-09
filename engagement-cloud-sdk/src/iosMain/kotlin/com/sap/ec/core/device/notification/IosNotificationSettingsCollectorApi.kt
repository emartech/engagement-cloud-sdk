package com.sap.ec.core.device.notification

internal interface IosNotificationSettingsCollectorApi {

    suspend fun collect(): IosNotificationSettings

}