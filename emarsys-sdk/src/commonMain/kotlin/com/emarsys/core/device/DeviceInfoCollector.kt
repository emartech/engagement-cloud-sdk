package com.emarsys.core.device

expect class DeviceInfoCollector: DeviceInfoCollectorApi {
    override fun collect(): String

    override fun getClientId(): String

    override suspend fun getPushSettings(): PushSettings
}