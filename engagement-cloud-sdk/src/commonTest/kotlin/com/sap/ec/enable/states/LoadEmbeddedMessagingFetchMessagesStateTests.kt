package com.sap.ec.enable.states

import com.sap.ec.context.Features
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.channel.SdkEventEmitterApi
import com.sap.ec.core.log.Logger
import com.sap.ec.event.SdkEvent
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class LoadEmbeddedMessagingFetchMessagesStateTests {
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var mockSdkLogger: Logger
    private lateinit var mockSdkEventEmitter: SdkEventEmitterApi
    private lateinit var state: LoadEmbeddedMessagingFetchMessagesState

    @BeforeTest
    fun setup() {
        mockSdkLogger = mock(MockMode.autofill)
        mockSdkEventEmitter = mock(MockMode.autofill)
        mockSdkContext = mock(MockMode.autofill)

        state = LoadEmbeddedMessagingFetchMessagesState(
            sdkEventEmitter = mockSdkEventEmitter,
            sdkContext = mockSdkContext,
            sdkLogger = mockSdkLogger
        )
    }

    @Test
    fun testActive_shouldEmitTriggerRefreshEvent_whenEmbeddedMessagingEnabled() =
        runTest {
            every { mockSdkContext.features } returns mutableSetOf(Features.EmbeddedMessaging)
            everySuspend { mockSdkEventEmitter.emitEvent(any()) } returns Unit

            val result = state.active()

            result shouldBe Result.success(Unit)
            verifySuspend { mockSdkEventEmitter.emitEvent(any<SdkEvent.Internal.EmbeddedMessaging.TriggerRefresh>()) }
        }

    @Test
    fun testActive_shouldNotEmitTriggerRefreshEvent_whenEmbeddedMessagingDisabled() =
        runTest {
            every { mockSdkContext.features } returns mutableSetOf()

            val result = state.active()

            result shouldBe Result.success(Unit)
            verifySuspend(VerifyMode.exactly(0)) { mockSdkEventEmitter.emitEvent(any<SdkEvent.Internal.EmbeddedMessaging.TriggerRefresh>()) }
        }
}
