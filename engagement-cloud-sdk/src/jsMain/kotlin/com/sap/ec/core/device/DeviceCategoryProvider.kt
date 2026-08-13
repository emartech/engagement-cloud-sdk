package com.sap.ec.core.device

import com.sap.ec.core.log.Logger
import com.sap.ec.npm_dependencies.bowser.parseUserAgent

internal class DeviceCategoryProvider(
    private val userAgent: String,
    private val userAgentData: dynamic,
    private val sdkLogger: Logger
) : DeviceCategoryProviderApi {

    override suspend fun getDeviceCategory(): DeviceCategory {
        try {
            val type =
                parseUserAgent(
                    userAgent,
                    userAgentData
                ).platform?.type

            if (type != null) {
                return DeviceCategory.valueOf(type.uppercase())
            }
        } catch (e: Exception) {
            sdkLogger.info("determining device category failed", e)
        }
        return DeviceCategory.UNKNOWN
    }
}
