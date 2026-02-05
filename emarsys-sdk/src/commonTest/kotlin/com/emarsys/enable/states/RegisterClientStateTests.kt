package com.emarsys.enable.states

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.channel.SdkEventWaiterApi
import com.emarsys.event.SdkEvent
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture.Companion.slot
import dev.mokkery.matcher.capture.SlotCapture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterClientStateTests {
    private lateinit var mockEventDistributor: SdkEventDistributorApi
    private lateinit var eventSlot: SlotCapture<SdkEvent>
    private lateinit var registerClientState: RegisterClientState

    private lateinit var mockWaiter: SdkEventWaiterApi

    @BeforeTest
    fun setup() = runTest {
        mockWaiter = mock()
        everySuspend { mockWaiter.await<Any>() } returns SdkEvent.Internal.Sdk.Answer.Response(
            "0",
            Result.success(Unit)
        )
        mockEventDistributor = mock()
        eventSlot = slot()
        everySuspend { mockEventDistributor.registerEvent(capture(eventSlot)) } returns mockWaiter
        registerClientState = RegisterClientState(mockEventDistributor)
    }

    @Test
    fun testActive_should_callDeviceClient() = runTest {
        val result = registerClientState.active()

        advanceUntilIdle()

        result shouldBe Result.success(Unit)
        verifySuspend {
            mockEventDistributor.registerEvent(any())
        }

        (eventSlot.get() is SdkEvent.Internal.Sdk.RegisterDeviceInfo) shouldBe true
    }

    @Test
    fun testActive_should_callDeviceClient_andReturnFailureResult_whenRequestFails() = runTest {
        val testException = Exception("test exception")
        everySuspend { mockWaiter.await<Any>() } returns SdkEvent.Internal.Sdk.Answer.Response(
            "0",
            Result.failure(testException)
        )

        val result = registerClientState.active()

        advanceUntilIdle()

        result.isSuccess shouldBe false
        result.exceptionOrNull() shouldBe testException
        verifySuspend {
            mockEventDistributor.registerEvent(any())
        }

        (eventSlot.get() is SdkEvent.Internal.Sdk.RegisterDeviceInfo) shouldBe true
    }
}