package com.emarsys.enable.states

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.networking.clients.event.model.SdkEvent
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.capture.Capture.Companion.slot
import dev.mokkery.matcher.capture.SlotCapture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class ApplyAppCodeBasedRemoteConfigStateTests {
    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi
    private lateinit var eventSlot: SlotCapture<SdkEvent>
    private lateinit var applyAppCodeBasedRemoteConfigState: ApplyAppCodeBasedRemoteConfigState

    @BeforeTest
    fun setUp() {
        eventSlot = slot()
        mockSdkEventDistributor = mock()
        applyAppCodeBasedRemoteConfigState =
            ApplyAppCodeBasedRemoteConfigState(mockSdkEventDistributor)
    }

    @Test
    fun testName() = runTest {
        applyAppCodeBasedRemoteConfigState.name shouldBe "applyAppCodeBasedRemoteConfig"
    }

    @Test
    fun testActive() = runTest {
        everySuspend { mockSdkEventDistributor.registerEvent(capture(eventSlot)) } returns mock(
            MockMode.autofill)

        applyAppCodeBasedRemoteConfigState.active()

        (eventSlot.get() is SdkEvent.Internal.Sdk.ApplyAppCodeBasedRemoteConfig) shouldBe true
    }
}