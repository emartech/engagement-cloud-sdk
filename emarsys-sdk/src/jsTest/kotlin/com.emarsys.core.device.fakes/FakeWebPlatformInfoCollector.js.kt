package com.emarsys.core.device.fakes

import com.emarsys.core.device.PlatformInfoCollectorApi

class FakeWebPlatformInfoCollector(private val onCollectCalled: (() -> String)? = null): PlatformInfoCollectorApi {

    override fun collect(): String {
        return onCollectCalled?.let { it() } ?: ""
    }
}