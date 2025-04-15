package com.emarsys.init.states

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.log.ConsoleLogger
import com.emarsys.core.log.SdkLogger
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


class ApplyGlobalRemoteConfigStateTests {
    private lateinit var applyGlobalRemoteConfigState: ApplyGlobalRemoteConfigState
    private lateinit var eventSlot: SlotCapture<SdkEvent>
    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi

    @BeforeTest
    fun setup() {
        eventSlot = slot()
        mockSdkEventDistributor = mock()

        applyGlobalRemoteConfigState = ApplyGlobalRemoteConfigState(
            mockSdkEventDistributor,
            SdkLogger("TestLoggerName", ConsoleLogger(), sdkContext = mock())
        )
    }

    @Test
    fun testName() = runTest {
        applyGlobalRemoteConfigState.name shouldBe "applyGlobalRemoteConfig"
    }

    @Test
    fun testActive_should_handleGlobal_with_remoteConfigHandler() = runTest {
        everySuspend { mockSdkEventDistributor.registerEvent(capture(eventSlot)) } returns mock(
            MockMode.autofill)

        applyGlobalRemoteConfigState.active()

        (eventSlot.get() is SdkEvent.Internal.Sdk.ApplyGlobalRemoteConfig) shouldBe true
    }
}