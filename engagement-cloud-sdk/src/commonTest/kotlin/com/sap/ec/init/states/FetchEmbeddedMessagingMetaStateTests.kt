package com.sap.ec.init.states

import com.sap.ec.context.Features
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.channel.SdkEventWaiterApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.model.Response
import com.sap.ec.enable.states.FetchEmbeddedMessagingMetaState
import com.sap.ec.event.SdkEvent
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class FetchEmbeddedMessagingMetaStateTests {
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var mockSdkLogger: Logger
    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi
    private lateinit var mockSdkEventWaiter: SdkEventWaiterApi
    private lateinit var state: FetchEmbeddedMessagingMetaState

    @BeforeTest
    fun setup() {
        mockSdkLogger = mock(MockMode.autofill)
        mockSdkEventDistributor = mock(MockMode.autofill)
        mockSdkEventWaiter = mock(MockMode.autofill)
        mockSdkContext = mock(MockMode.autofill)

        state = FetchEmbeddedMessagingMetaState(
            sdkEventDistributor = mockSdkEventDistributor,
            sdkContext = mockSdkContext,
            sdkLogger = mockSdkLogger
        )
    }

    @Test
    fun testActive_shouldRegisterFetchMetaEvent_andReturnSuccess_whenEmbeddedMessagingIsEnabled() =
        runTest {
            every { mockSdkContext.features } returns mutableSetOf(Features.EmbeddedMessaging)
            everySuspend {
                mockSdkEventDistributor.registerEvent(any<SdkEvent.Internal.EmbeddedMessaging.FetchMeta>())
            } returns mockSdkEventWaiter
            everySuspend { mockSdkEventWaiter.await<Response>(10.seconds) } returns
                SdkEvent.Internal.Sdk.Answer.Response("0", Result.failure(Exception()))

            val result = state.active()

            result shouldBe Result.success(Unit)
            verifySuspend {
                mockSdkEventDistributor.registerEvent(any<SdkEvent.Internal.EmbeddedMessaging.FetchMeta>())
            }
            verifySuspend {
                mockSdkEventWaiter.await<Response>(10.seconds)
            }
        }

    @Test
    fun testActive_shouldNotRegisterFetchMetaEvent_andReturnSuccess_whenEmbeddedMessagingIsDisabled() =
        runTest {
            every { mockSdkContext.features } returns mutableSetOf()

            val result = state.active()

            result shouldBe Result.success(Unit)
            verifySuspend(VerifyMode.exactly(0)) {
                mockSdkEventDistributor.registerEvent(any<SdkEvent.Internal.EmbeddedMessaging.FetchMeta>())
            }
        }
}
