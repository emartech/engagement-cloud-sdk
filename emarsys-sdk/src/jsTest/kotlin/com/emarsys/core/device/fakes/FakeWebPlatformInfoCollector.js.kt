package com.emarsys.core.device.fakes

import com.emarsys.core.device.WebPlatformInfo
import com.emarsys.core.device.WebPlatformInfoCollectorApi

class FakeWebPlatformInfoCollector(private val fakeValue: WebPlatformInfo? = null): WebPlatformInfoCollectorApi {
    override fun collect(): WebPlatformInfo {
        return fakeValue ?: WebPlatformInfo(null, false, "unknown", "0.0.0", "unknown", "0.0.0")
    }
}