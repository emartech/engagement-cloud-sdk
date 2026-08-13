package com.sap.ec.core.device

internal interface WebPlatformInfoCollectorApi {
    suspend fun collect(): WebPlatformInfo
}