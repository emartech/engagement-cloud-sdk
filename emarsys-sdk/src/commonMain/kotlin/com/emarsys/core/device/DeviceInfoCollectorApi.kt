package com.emarsys.core.device

interface DeviceInfoCollectorApi {
    fun collect(): String

    fun getClientId(): String

    suspend fun getPushSettings(): PushSettings
}