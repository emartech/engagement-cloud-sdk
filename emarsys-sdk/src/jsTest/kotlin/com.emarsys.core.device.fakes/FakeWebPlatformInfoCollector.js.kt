package com.emarsys.core.device.fakes

import com.emarsys.core.device.PlatformInfoCollectorApi
import com.emarsys.core.device.UNKNOWN_VERSION_NAME

class FakeWebPlatformInfoCollector(private val onCollectCalled: (() -> String)? = null): PlatformInfoCollectorApi {

    override fun collect(): String {
        return onCollectCalled?.let { it() } ?: ""
    }

    override fun applicationVersion(): String = UNKNOWN_VERSION_NAME
}