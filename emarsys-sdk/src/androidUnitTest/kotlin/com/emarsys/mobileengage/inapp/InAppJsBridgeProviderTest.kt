package com.emarsys.mobileengage.inapp

import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.models.ActionModel
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class InAppJsBridgeProviderTest {

    @Test
    fun provide_shouldReturn_inAppJsBridgeApi() = runTest {
        val mockActionFactory: ActionFactoryApi<ActionModel> = mockk(relaxed = true)
        val inAppJsBridgeProvider = InAppJsBridgeProvider(mockActionFactory)

        inAppJsBridgeProvider.provide()::class.java shouldBe InAppJsBridge::class.java
    }
}