package com.sap.ec.networking.clients.recommendation

import com.sap.ec.core.channel.SdkEventManagerApi
import com.sap.ec.core.db.events.EventsDaoApi
import com.sap.ec.core.exceptions.SdkException
import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.clients.NetworkClientApi
import com.sap.ec.core.networking.model.Response
import com.sap.ec.core.networking.model.UrlRequest
import com.sap.ec.event.OnlineSdkEvent
import com.sap.ec.event.SdkEvent
import com.sap.ec.mobileengage.recommendation.RecommendationConstants.CART_LIST_ITEM_QUANTITY_KEY
import com.sap.ec.mobileengage.recommendation.networking.RecommendationRequestFactoryApi
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import kotlinx.coroutines.CoroutineScope
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

    private companion object {
        private const val RECOMMENDATION_BASE_URL =
            "https://recommender.scarabresearch.com/merchants/"
    }

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
    fun testConsumer_shouldConsumeWebExtendEvent_and_sendTheRequestToTheBackend_andAckEvent_whenReceivingSuccessResponse() =
        runTest {
            createRecommendationClient(backgroundScope).register()
            val searchEvent = SdkEvent.External.WebExtendEvent.Search("testSearchTerm")
            val request = UrlRequest(
                url = Url("$RECOMMENDATION_BASE_URL?$CART_LIST_ITEM_QUANTITY_KEY=testSearchTerm"),
                method = HttpMethod.Get
            )
            val successResponse = Response(
                status = HttpStatusCode.OK,
                headers = Headers.Empty,
                originalRequest = request,
                bodyAsText = "{}"
            )
            val successResult = Result.success(successResponse)
            everySuspend { mockRecommendationRequestFactory.create(searchEvent) } returns request
            everySuspend { mockNetworkClient.send(request) } returns successResult

            val onlineSdkEvents = backgroundScope.async {
                onlineEvents.take(1).toList()
            }

            onlineEvents.emit(searchEvent)

            advanceUntilIdle()

            onlineSdkEvents.await().size shouldBe 1
            verifySuspend {
                mockSdkLogger.debug("consume RecommendationClient events")
                mockRecommendationRequestFactory.create(searchEvent)
                mockNetworkClient.send(request)
                mockSdkEventManager.emitEvent(
                    SdkEvent.Internal.Sdk.Answer.Response(
                        searchEvent.id,
                        successResult
                    )
                )
                mockEventsDao.removeEvent(searchEvent)
            }
        }

    @Test
    fun testConsumer_shouldConsumeWebExtendEvent_and_sendTheRequestToTheBackend_andAckEvent_whenReceivingFailureResponse() =
        runTest {
            createRecommendationClient(backgroundScope).register()
            val searchEvent = SdkEvent.External.WebExtendEvent.Search("testSearchTerm")
            val request = UrlRequest(
                url = Url("$RECOMMENDATION_BASE_URL?$CART_LIST_ITEM_QUANTITY_KEY=testSearchTerm"),
                method = HttpMethod.Get
            )
            val failureResponse = Response(
                status = HttpStatusCode.BadRequest,
                headers = Headers.Empty,
                originalRequest = request,
                bodyAsText = "{}"
            )
            val failureResult =
                Result.failure<Response>(SdkException.FailedRequestException(failureResponse))
            everySuspend { mockRecommendationRequestFactory.create(searchEvent) } returns request
            everySuspend { mockNetworkClient.send(request) } returns failureResult

            val onlineSdkEvents = backgroundScope.async {
                onlineEvents.take(1).toList()
            }

            onlineEvents.emit(searchEvent)

            advanceUntilIdle()

            onlineSdkEvents.await().size shouldBe 1
            verifySuspend {
                mockSdkLogger.debug("consume RecommendationClient events")
                mockRecommendationRequestFactory.create(searchEvent)
                mockNetworkClient.send(request)

                mockSdkEventManager.emitEvent(
                    SdkEvent.Internal.Sdk.Answer.Response(
                        searchEvent.id,
                        failureResult
                    )
                )
                mockEventsDao.removeEvent(searchEvent)
            }
        }

    private fun createRecommendationClient(applicationScope: CoroutineScope): RecommendationClient {
        return RecommendationClient(
            mockSdkEventManager,
            applicationScope,
            mockNetworkClient,
            mockRecommendationRequestFactory,
            mockEventsDao,
            mockSdkLogger
        )
    }
}