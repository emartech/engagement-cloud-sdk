package com.emarsys.core.device.fakes

import com.emarsys.core.providers.Provider

class FakeStringProvider(private val fakeValue: String): Provider<String> {
    override fun provide(): String {
        return fakeValue
    }
}