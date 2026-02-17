package com.sap.ec.core.device.fakes

import com.sap.ec.core.providers.Provider

class FakeStringProvider(private val fakeValue: String): Provider<String> {
    override fun provide(): String {
        return fakeValue
    }
}