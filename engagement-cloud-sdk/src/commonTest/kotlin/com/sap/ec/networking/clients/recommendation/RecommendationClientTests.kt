package com.sap.ec.networking.clients.recommendation

import com.sap.ec.core.channel.SdkEventManagerApi
import com.sap.ec.core.db.events.EventsDaoApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.clients.NetworkClientApi
import com.sap.ec.event.OnlineSdkEvent
import com.sap.ec.event.SdkEvent
import com.sap.ec.mobileengage.recommendation.networking.RecommendationRequestFactoryApi
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecommendationClientTests {

    private lateinit var mockSdkLogger: Logger
    private lateinit var mockSdkEventManager: SdkEventManagerApi
    private lateinit var mockEventsDao: EventsDaoApi
    private lateinit var mockNetworkClient: NetworkClientApi
    private lateinit var mockRecommendationRequestFactory: RecommendationRequestFactoryApi
    private lateinit var onlineEvents: MutableSharedFlow<OnlineSdkEvent>

    @BeforeTest
    fun setup() = runTest {
        Dispatchers.setMain(StandardTestDispatcher())
        mockSdkLogger = mock(MockMode.autofill)
        mockSdkEventManager = mock(MockMode.autofill)
        mockEventsDao = mock(MockMode.autofill)
        mockNetworkClient = mock(MockMode.autofill)
        mockRecommendationRequestFactory = mock(MockMode.autofill)

        onlineEvents = MutableSharedFlow(replay = 100, extraBufferCapacity = Channel.UNLIMITED)

        everySuspend { mockSdkLogger.debug(any<String>()) }
        everySuspend { mockSdkEventManager.onlineSdkEvents } returns onlineEvents
    }

    @Test
    fun testConsumer_shouldConsumeWebExtendEventsOnly() = runTest {
        RecommendationClient(mockSdkEventManager, backgroundScope, mockNetworkClient, mockRecommendationRequestFactory, mockSdkLogger).register()
        val relevantEvent = SdkEvent.External.WebExtendEvent.Search("testData")
        val notRelevantEvent = SdkEvent.Internal.EmbeddedMessaging.FetchMeta()

        val onlineSdkEvents = backgroundScope.async {
            onlineEvents.take(2).toList()
        }

        onlineEvents.emit(relevantEvent)
        onlineEvents.emit(notRelevantEvent)

        advanceUntilIdle()

        onlineSdkEvents.await().size shouldBe 2
        verifySuspend(VerifyMode.exactly(1)) { mockSdkLogger.debug("consume RecommendationClient events") }
    }
}