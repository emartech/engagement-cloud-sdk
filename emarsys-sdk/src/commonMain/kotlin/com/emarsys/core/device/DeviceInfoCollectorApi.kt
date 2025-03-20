package com.emarsys.core.device

interface DeviceInfoCollectorApi {
    suspend fun collect(): String

    suspend fun getClientId(): String

    suspend fun getPushSettings(): PushSettings
}