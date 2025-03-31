package com.emarsys.core.device

expect class DeviceInfoCollector: DeviceInfoCollectorApi {
    override suspend fun collect(): String

    override suspend fun collectAsDeviceInfo(): DeviceInfo

    override suspend fun collectAsDeviceInfoForLogs(): DeviceInfoForLogs

    override suspend fun getClientId(): String

    override suspend fun getPushSettings(): PushSettings
}