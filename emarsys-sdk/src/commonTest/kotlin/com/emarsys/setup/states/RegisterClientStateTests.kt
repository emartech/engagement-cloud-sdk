package com.emarsys.setup.states

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.networking.clients.event.model.SdkEvent
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

    @BeforeTest
    fun setup() = runTest {
        mockEventDistributor = mock()
        eventSlot = slot()
        everySuspend { mockEventDistributor.registerAndStoreEvent(capture(eventSlot)) } returns Unit
        registerClientState = RegisterClientState(mockEventDistributor)
    }

    @Test
    fun testActive_should_callDeviceClient() = runTest {
        registerClientState.active()

        advanceUntilIdle()

        verifySuspend {
            mockEventDistributor.registerAndStoreEvent(any())
        }

        (eventSlot.get() is SdkEvent.Internal.Sdk.RegisterDeviceInfo) shouldBe true
    }
}