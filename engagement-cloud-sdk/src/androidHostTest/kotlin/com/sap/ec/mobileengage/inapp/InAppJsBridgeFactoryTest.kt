package com.sap.ec.mobileengage.inapp

import com.sap.ec.mobileengage.action.EventActionFactoryApi
import com.sap.ec.mobileengage.inapp.jsbridge.InAppJsBridgeData
import com.sap.ec.util.JsonUtil
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class InAppJsBridgeFactoryTest {
    private companion object {
        const val DISMISS_ID = "dismissId"
        const val TRACKING_INFO = """{"key1":"value1","key2":"value2"}"""
    }

    @Test
    fun provide_shouldReturn_inAppJsBridgeApi() = runTest {
        val mockActionFactory: EventActionFactoryApi = mockk(relaxed = true)

        val inAppJsBridgeProvider = InAppJsBridgeFactory(
            mockActionFactory,
            JsonUtil.json,
            TestScope(StandardTestDispatcher())
        )

        inAppJsBridgeProvider.create(
            InAppJsBridgeData(
                DISMISS_ID,
                TRACKING_INFO
            )
        )::class.java shouldBe InAppJsBridge::class.java
    }
}