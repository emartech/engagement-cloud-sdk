package com.sap.ec.core.device

import com.sap.ec.core.device.constants.BrowserInfo
import com.sap.ec.core.device.constants.OsInfo
import com.sap.ec.core.log.Logger
import com.sap.ec.npm_dependencies.parseUserAgent

internal class WebPlatformInfoCollector(
    private val navigatorData: String,
    private val userAgent: String,
    private val userAgentData: dynamic,
    private val sdkLogger: Logger
) :
    WebPlatformInfoCollectorApi {
    private companion object {
        const val DEFAULT_BROWSER_VERSION = "0"
    }

    override suspend fun collect(): WebPlatformInfo {
        val headerData = analiseHeaders()
        return WebPlatformInfo(
            null,
            false,
            headerData.osName,
            headerData.osVersion,
            headerData.browserName.lowercase(),
            headerData.browserVersion,
            headerData.deviceCategory
        )
    }

    private suspend fun analiseHeaders(): WindowHeaderData {
        val osInfo = OsInfo.entries.firstOrNull {
            navigatorData.contains(it.value)
        } ?: OsInfo.Unknown
        val osVersion = extractBrowserVersionNumber(osInfo.versionPrefix)

        val browserInfo = BrowserInfo.entries.firstOrNull {
            navigatorData.contains(it.value)
        } ?: BrowserInfo.Unknown
        val browserVersion = extractBrowserVersionNumber(browserInfo.versionPrefix)

        val deviceCategory = getDeviceCategory()

        return WindowHeaderData(
            osInfo.name,
            osVersion,
            browserInfo.name,
            browserVersion,
            deviceCategory
        )
    }

    private fun extractBrowserVersionNumber(versionPrefix: String): String {
        val versionRegex = Regex("""$versionPrefix[- /:;]([\d._]+)""")
        val versionMatches = versionRegex.findAll(navigatorData).firstOrNull()?.groupValues
        return if (!versionMatches.isNullOrEmpty()) {
            val versionNumbers = versionMatches.drop(1)
            versionNumbers.first().replace("_", ".")
        } else DEFAULT_BROWSER_VERSION
    }

    private suspend fun getDeviceCategory(): DeviceCategory {
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