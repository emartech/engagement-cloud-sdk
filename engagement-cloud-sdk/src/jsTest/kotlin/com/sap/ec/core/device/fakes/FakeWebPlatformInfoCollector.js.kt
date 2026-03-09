package com.sap.ec.core.device.fakes

import com.sap.ec.core.device.WebPlatformInfo
import com.sap.ec.core.device.WebPlatformInfoCollectorApi

internal class FakeWebPlatformInfoCollector(private val fakeValue: WebPlatformInfo? = null): WebPlatformInfoCollectorApi {
    override fun collect(): WebPlatformInfo {
        return fakeValue ?: WebPlatformInfo(null, false, "unknown", "0.0.0", "unknown", "0.0.0")
    }
}