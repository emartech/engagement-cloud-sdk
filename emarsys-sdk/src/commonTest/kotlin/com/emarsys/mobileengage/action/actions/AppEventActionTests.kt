package com.emarsys.mobileengage.action.actions

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.channel.SdkEventWaiterApi
import com.emarsys.event.SdkEvent
import com.emarsys.mobileengage.action.models.BasicAppEventActionModel
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.matcher.capture.SlotCapture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class AppEventActionTests {
    private companion object {
        const val TEST_NAME = "testName"
    }

    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi
    private lateinit var mockSdkEventWaiter: SdkEventWaiterApi
    private lateinit var eventSlot: SlotCapture<SdkEvent.External.Api.AppEvent>

    @BeforeTest
    fun setup() {
        eventSlot = Capture.slot()
        mockSdkEventWaiter = mock(MockMode.autoUnit)
        everySuspend { mockSdkEventWaiter.await<Any>() } returns SdkEvent.Internal.Sdk.Answer.Response(
            "0",
            Result.success(Any())
        )
        mockSdkEventDistributor = mock(MockMode.autofill)
        everySuspend { mockSdkEventDistributor.registerEvent(capture(eventSlot)) } returns mockSdkEventWaiter
    }

    @Test
    fun invoke_shouldNotAddAttributes_toInvokedAction_ifActionModelPayload_isNull() = runTest {
        val testActionModel = BasicAppEventActionModel(name = TEST_NAME, payload = null)

        AppEventAction(testActionModel, mockSdkEventDistributor).invoke()

        advanceUntilIdle()

        eventSlot.get().attributes shouldBe null
    }

    @Test
    fun invoke_shouldAddAttributes_toInvokedAction_ifActionModelPayload_isNotNull() = runTest {
        val testAttributes = mapOf("key" to "value", "testKey" to "testValue")
        val testActionModel = BasicAppEventActionModel(name = TEST_NAME, payload = testAttributes)

        AppEventAction(testActionModel, mockSdkEventDistributor).invoke()

        advanceUntilIdle()

        eventSlot.get().attributes shouldBe buildJsonObject {
            put("key", "value")
            put("testKey", "testValue")
        }
    }
}
