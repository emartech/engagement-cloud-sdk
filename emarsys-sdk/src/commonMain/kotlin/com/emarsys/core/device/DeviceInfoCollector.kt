package com.emarsys.core.device

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
internal expect class DeviceInfoCollector: DeviceInfoCollectorApi {
    override suspend fun collect(): String

    override suspend fun collectAsDeviceInfo(): DeviceInfo

    override suspend fun collectAsDeviceInfoForLogs(): DeviceInfoForLogs

    override suspend fun getClientId(): String

    override suspend fun getPushSettings(): PushSettings
}