package com.emarsys.mobileengage.inapp

import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.util.JsonUtil
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class InAppJsBridgeProviderTest {

    @Test
    fun provide_shouldReturn_inAppJsBridgeApi() = runTest {
        val mockActionFactory: ActionFactoryApi<ActionModel> = mockk(relaxed = true)
        val inAppJsBridgeProvider = InAppJsBridgeProvider(
            mockActionFactory,
            JsonUtil.json,
            CoroutineScope(StandardTestDispatcher())
        )

        inAppJsBridgeProvider.provide()::class.java shouldBe InAppJsBridge::class.java
    }
}