package com.emarsys.core.device

interface DeviceInfoCollectorApi {
    suspend fun collect(): String

    suspend fun collectAsDeviceInfo(): DeviceInfo

    suspend fun collectAsDeviceInfoForLogs(): DeviceInfoForLogs

    suspend fun getClientId(): String

    suspend fun getNotificationSettings(): NotificationSettings
}