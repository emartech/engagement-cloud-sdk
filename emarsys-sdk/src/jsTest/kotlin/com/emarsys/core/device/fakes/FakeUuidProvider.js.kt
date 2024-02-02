package com.emarsys.core.device.fakes

import com.emarsys.core.providers.Provider

class FakeUuidProvider(private val fakeValue: String = "fake uuid"): Provider<String> {

    override fun provide(): String {
        return fakeValue
    }
}