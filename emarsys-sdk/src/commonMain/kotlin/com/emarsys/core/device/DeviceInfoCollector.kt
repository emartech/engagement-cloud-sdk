package com.emarsys.core.device

expect class DeviceInfoCollector: DeviceInfoCollectorApi {
    override suspend fun collect(): String

    override suspend fun getClientId(): String

    override suspend fun getPushSettings(): PushSettings
}