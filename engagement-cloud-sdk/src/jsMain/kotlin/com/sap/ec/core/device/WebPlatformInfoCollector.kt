package com.sap.ec.core.device

import com.sap.ec.core.device.constants.BrowserInfo
import com.sap.ec.core.device.constants.OsInfo

internal class WebPlatformInfoCollector(
    private val navigatorData: String,
    private val deviceCategoryProvider: DeviceCategoryProviderApi
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

        val deviceCategory = deviceCategoryProvider.getDeviceCategory()

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
}
