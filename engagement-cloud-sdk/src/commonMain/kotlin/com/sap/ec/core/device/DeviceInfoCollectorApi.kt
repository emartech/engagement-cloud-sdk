package com.sap.ec.core.device

internal interface DeviceInfoCollectorApi {
    suspend fun collect(): String

    suspend fun collectAsDeviceInfo(): DeviceInfo

    suspend fun collectAsDeviceInfoForLogs(): DeviceInfoForLogs

    suspend fun getClientId(): String

    suspend fun getNotificationSettings(): NotificationSettings

    fun getPlatformCategory(): String
}