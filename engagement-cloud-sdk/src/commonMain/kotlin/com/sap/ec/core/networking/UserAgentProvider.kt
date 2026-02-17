package com.sap.ec.core.networking

import com.sap.ec.core.device.DeviceInfo
import com.sap.ec.core.device.DeviceInfoCollectorApi
import kotlinx.serialization.json.Json

internal class UserAgentProvider(private val deviceInfoCollector: DeviceInfoCollectorApi, private val json: Json): UserAgentProviderApi {

    companion object {
        const val USER_AGENT_HEADER_NAME = "User-Agent"
    }

    override suspend fun provide(): String {
        val deviceInfoString = deviceInfoCollector.collect()
        val deviceInfo: DeviceInfo = json.decodeFromString(deviceInfoString)
        return "Engagement Cloud SDK ${deviceInfo.sdkVersion} ${deviceInfo.platform} ${deviceInfo.osVersion}"
    }
}