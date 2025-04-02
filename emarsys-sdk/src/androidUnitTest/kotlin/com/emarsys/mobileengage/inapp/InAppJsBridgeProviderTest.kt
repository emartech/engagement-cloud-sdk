package com.emarsys.mobileengage.inapp

import com.emarsys.mobileengage.action.EventActionFactoryApi
import com.emarsys.util.JsonUtil
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class InAppJsBridgeProviderTest {
    private companion object {
        const val CAMPAIGN_ID = "campaignId"
    }

    @Test
    fun provide_shouldReturn_inAppJsBridgeApi() = runTest {
        val mockActionFactory: EventActionFactoryApi = mockk(relaxed = true)
        val inAppJsBridgeProvider = InAppJsBridgeProvider(
            mockActionFactory,
            JsonUtil.json,
            StandardTestDispatcher()
        )

        inAppJsBridgeProvider.create(CAMPAIGN_ID)::class.java shouldBe InAppJsBridge::class.java
    }
}