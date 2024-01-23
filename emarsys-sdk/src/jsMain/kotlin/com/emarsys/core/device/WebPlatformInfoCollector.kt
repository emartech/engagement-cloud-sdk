package com.emarsys.core.device

import com.emarsys.core.device.constants.BrowserInfo
import com.emarsys.core.device.constants.OsInfo
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WebPlatformInfoCollector(private val navigatorData: String) : PlatformInfoCollectorApi {
    private companion object {
        const val DEFAULT_BROWSER_VERSION = "0"
    }

    override fun collect(): String {
        val headerData = analiseHeaders()
        val webPlatformInfo =
            WebPlatformInfo(
                null,
                false,
                headerData.osName,
                headerData.osVersion,
                headerData.browserName,
                headerData.browserVersion
            )
        return Json.encodeToString(webPlatformInfo)
    }

    override fun applicationVersion(): String {
        return UNKNOWN_VERSION_NAME
    }

    private fun analiseHeaders(): WindowHeaderData {
        val osInfo = OsInfo.entries.firstOrNull {
            navigatorData.contains(it.value)
        } ?: OsInfo.Unknown
        val osVersion = extractBrowserVersionNumber(osInfo.versionPrefix)

        val browserInfo = BrowserInfo.entries.firstOrNull {
            navigatorData.contains(it.value)
        } ?: BrowserInfo.Unknown
        val browserVersion = extractBrowserVersionNumber(browserInfo.versionPrefix)

        return WindowHeaderData(osInfo.name, osVersion, browserInfo.name, browserVersion)
    }

    private fun extractBrowserVersionNumber(versionPrefix: String): String {
        val versionRegex = Regex("""$versionPrefix[- /:;]([\d._]+)""")
        val versionMatches = versionRegex.findAll(navigatorData).firstOrNull()?.groupValues
        return if (!versionMatches.isNullOrEmpty()) {
            val versionNumbers = versionMatches.drop(1)
            return versionNumbers.first().replace("_", ".")
        } else DEFAULT_BROWSER_VERSION
    }
}