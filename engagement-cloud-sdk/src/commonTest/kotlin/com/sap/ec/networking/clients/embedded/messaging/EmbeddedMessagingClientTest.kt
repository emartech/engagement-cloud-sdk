package com.sap.ec.networking.clients.embedded.messaging

import com.sap.ec.core.channel.SdkEventManagerApi
import com.sap.ec.core.db.events.EventsDaoApi
import com.sap.ec.core.exceptions.SdkException
import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.clients.NetworkClientApi
import com.sap.ec.core.networking.model.Response
import com.sap.ec.core.networking.model.UrlRequest
import com.sap.ec.core.networking.model.body
import com.sap.ec.event.OnlineSdkEvent
import com.sap.ec.event.SdkEvent
import com.sap.ec.mobileengage.embeddedmessaging.EmbeddedMessagingContext
import com.sap.ec.mobileengage.embeddedmessaging.EmbeddedMessagingContextApi
import com.sap.ec.mobileengage.embeddedmessaging.networking.EmbeddedMessagingRequestFactoryApi
import com.sap.ec.networking.clients.embedded.messaging.model.MetaData
import com.sap.ec.networking.clients.error.ClientExceptionHandler
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.answering.sequentiallyReturns
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.resetAnswers
import dev.mokkery.resetCalls
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class EmbeddedMessagingClientTest {
    private lateinit var mockSdkLogger: Logger
    private lateinit var mockSdkEventManager: SdkEventManagerApi
    private lateinit var mockEventsDao: EventsDaoApi
    private lateinit var mockClientExceptionHandler: ClientExceptionHandler
    private lateinit var onlineEvents: MutableSharedFlow<OnlineSdkEvent>
    private lateinit var mockEmbeddedMessagesRequestFactory: EmbeddedMessagingRequestFactoryApi
    private lateinit var mockECNetworkClient: NetworkClientApi
    private lateinit var embeddedMessagingContext: EmbeddedMessagingContextApi

    @BeforeTest
    fun setup() = runTest {
        Dispatchers.setMain(StandardTestDispatcher())
        mockSdkLogger = mock(MockMode.autofill)
        mockSdkEventManager = mock()
        mockEventsDao = mock()
        mockEmbeddedMessagesRequestFactory = mock()
        mockClientExceptionHandler = mock()
        mockECNetworkClient = mock()
        embeddedMessagingContext = EmbeddedMessagingContext()

        onlineEvents = MutableSharedFlow()

        everySuspend {
            mockSdkLogger.debug(any<String>())
        }
        everySuspend { mockSdkEventManager.onlineSdkEvents } returns onlineEvents
    }

    @AfterTest
    fun tearDown() {
        resetCalls()
        resetAnswers()
    }

    private fun createEmbeddedMessagingClient(applicationScope: CoroutineScope) =
        EmbeddedMessagingClient(
            sdkLogger = mockSdkLogger,
            sdkEventManager = mockSdkEventManager,
            applicationScope = applicationScope,
            embeddedMessagingRequestFactory = mockEmbeddedMessagesRequestFactory,
            ecNetworkClient = mockECNetworkClient,
            eventsDao = mockEventsDao,
            clientExceptionHandler = mockClientExceptionHandler,
            embeddedMessagingContext = embeddedMessagingContext
        )

    @Test
    fun testConsumer_shouldGetFetchBadgeCountEventFromFlow_andSendHttpRequest_andAckEvent_andEmitResponse() =
        runTest {
            createEmbeddedMessagingClient(backgroundScope).register()
            val event = SdkEvent.Internal.EmbeddedMessaging.FetchBadgeCount(nackCount = 0)

            val request = UrlRequest(
                url = Url("https://test.com"),
                method = HttpMethod.Get
            )

            every {
                mockEmbeddedMessagesRequestFactory.create(
                    event
                )
            } returns request

            val expectedResponse = Response(
                status = HttpStatusCode.OK,
                headers = Headers.Empty,
                originalRequest = request,
                bodyAsText = "{}"
            )

            everySuspend { mockECNetworkClient.send(request) } returns Result.success(
                expectedResponse
            )
            everySuspend { mockEventsDao.removeEvent(event) } returns Unit
            everySuspend {
                mockSdkEventManager.emitEvent(
                    SdkEvent.Internal.Sdk.Answer.Response(
                        event.id,
                        Result.success(expectedResponse)
                    )
                )
            } returns Unit

            val onlineSdkEvents = backgroundScope.async {
                onlineEvents.take(1).toList()
            }

            onlineEvents.emit(event)

            advanceUntilIdle()

            onlineSdkEvents.await() shouldNotBe null
            verifySuspend {
                mockEmbeddedMessagesRequestFactory.create(
                    event
                )
            }
            verifySuspend { mockECNetworkClient.send(request) }
            verifySuspend { mockEventsDao.removeEvent(event) }
            verifySuspend {
                mockSdkEventManager.emitEvent(
                    SdkEvent.Internal.Sdk.Answer.Response(
                        event.id,
                        Result.success(expectedResponse)
                    )
                )
            }
        }

    @Test
    fun testConsumer_shouldConsumeOnlyEmbeddedMessagingEvents() = runTest {
        createEmbeddedMessagingClient(backgroundScope).register()
        val event1 = SdkEvent.Internal.EmbeddedMessaging.FetchBadgeCount(nackCount = 0)
        val event2 =
            SdkEvent.Internal.EmbeddedMessaging.FetchMessages(
                nackCount = 0,
                offset = 0,
                categoryIds = emptyList()
            )
        val event3 = SdkEvent.Internal.EmbeddedMessaging.FetchMeta(nackCount = 0)
        val event4 = SdkEvent.Internal.EmbeddedMessaging.UpdateTagsForMessages(
            nackCount = 0,
            updateData = emptyList()
        )
        val event5 = SdkEvent.Internal.EmbeddedMessaging.FetchNextPage(
            nackCount = 0,
            offset = 0,
            categoryIds = emptyList(),
            filterUnopenedMessages = false
        )
        val wrongEvent =
            SdkEvent.Internal.Sdk.LinkContact(
                contactFieldValue = "value",
                nackCount = 0
            )
        val onlineSdkEvents = backgroundScope.async {
            onlineEvents.take(6).toList()
        }
        val request = UrlRequest(
            url = Url("https://test.com"),
            method = HttpMethod.Get
        )
        val anyResponse = Response(
            status = HttpStatusCode.OK,
            headers = Headers.Empty,
            originalRequest = request,
            bodyAsText = "{}"
        )

        val metaDataResponse = Response(
            status = HttpStatusCode.OK,
            headers = Headers.Empty,
            originalRequest = request,
            bodyAsText = createMetaDataResponseString()
        )

        everySuspend { mockSdkEventManager.emitEvent(any()) } returns Unit
        everySuspend { mockEmbeddedMessagesRequestFactory.create(any()) } returns request
        everySuspend { mockECNetworkClient.send(any()) } sequentiallyReturns listOf(
            Result.success(anyResponse),
            Result.success(anyResponse),
            Result.success(metaDataResponse),
            Result.success(anyResponse),
            Result.success(anyResponse)
        )

        onlineEvents.emit(event1)
        onlineEvents.emit(event2)
        onlineEvents.emit(event3)
        onlineEvents.emit(event4)
        onlineEvents.emit(event5)
        onlineEvents.emit(wrongEvent)

        advanceUntilIdle()

        onlineSdkEvents.await() shouldBe listOf(event1, event2, event3, event4, event5, wrongEvent)
        verifySuspend(VerifyMode.exactly(5)) { mockEmbeddedMessagesRequestFactory.create(any()) }
    }

    @Test
    fun testConsumer_shouldReEmitEventsIntoFlow_whenThereIsANetworkError() = runTest {
        createEmbeddedMessagingClient(backgroundScope).register()
        val event = SdkEvent.Internal.EmbeddedMessaging.FetchBadgeCount(nackCount = 0)
        val request = UrlRequest(
            url = Url("https://test.com"),
            method = HttpMethod.Get
        )

        every {
            mockEmbeddedMessagesRequestFactory.create(
                event
            )
        } returns request

        everySuspend { mockECNetworkClient.send(request) } returns Result.failure(
            SdkException.NetworkIOException(
                "Test Network error"
            )
        )

        val onlineSdkEvents = backgroundScope.async {
            onlineEvents.take(1).toList()
        }

        everySuspend { mockSdkEventManager.emitEvent(any()) } returns Unit

        onlineEvents.emit(event)
        advanceUntilIdle()

        onlineSdkEvents.await() shouldBe listOf(event)
        verifySuspend { mockEmbeddedMessagesRequestFactory.create(event) }
        verifySuspend { mockECNetworkClient.send(request) }
        verifySuspend { mockSdkEventManager.emitEvent(event) }
    }

    @Test
    fun testConsumer_shouldCallClientExceptionHandler_whenExceptionHappens() = runTest {
        createEmbeddedMessagingClient(backgroundScope).register()
        val testException = Exception("Test Exception")
        val event = SdkEvent.Internal.EmbeddedMessaging.FetchBadgeCount(nackCount = 0)

        everySuspend { mockEmbeddedMessagesRequestFactory.create(event) } throws testException
        everySuspend {
            mockClientExceptionHandler.handleException(testException, any(), event)
        } returns Unit
        val onlineSdkEvents = backgroundScope.async {
            onlineEvents.take(1).toList()
        }
        everySuspend {
            mockSdkEventManager.emitEvent(
                SdkEvent.Internal.Sdk.Answer.Response(
                    event.id,
                    Result.failure<Exception>(testException)
                )
            )
        } returns Unit

        onlineEvents.emit(event)

        advanceUntilIdle()

        onlineSdkEvents.await() shouldBe listOf(event)
        verifySuspend(VerifyMode.exactly(0)) { mockECNetworkClient.send(any()) }
        verifySuspend {
            mockClientExceptionHandler.handleException(
                testException,
                any(),
                event
            )
        }
        verifySuspend {
            mockSdkEventManager.emitEvent(
                SdkEvent.Internal.Sdk.Answer.Response(
                    event.id,
                    Result.failure<Exception>(testException)
                )
            )
        }
    }

    @Test
    fun testConsumer_shouldGetFetchMetaEventFromFlow_andSendHttpRequest_andStoreMetaData_andAckEvent_andEmitResponse() =
        runTest {
            createEmbeddedMessagingClient(backgroundScope).register()
            val event = SdkEvent.Internal.EmbeddedMessaging.FetchMeta(nackCount = 0)

            val request = UrlRequest(
                url = Url("https://test.com"),
                method = HttpMethod.Get
            )

            every {
                mockEmbeddedMessagesRequestFactory.create(
                    event
                )
            } returns request

            val expectedResponse = Response(
                status = HttpStatusCode.OK,
                headers = Headers.Empty,
                originalRequest = request,
                bodyAsText = createMetaDataResponseString()
            )

            everySuspend { mockECNetworkClient.send(request) } returns Result.success(
                expectedResponse
            )
            everySuspend { mockEventsDao.removeEvent(event) } returns Unit
            everySuspend {
                mockSdkEventManager.emitEvent(
                    SdkEvent.Internal.Sdk.Answer.Response(
                        event.id,
                        Result.success(expectedResponse)
                    )
                )
            } returns Unit

            val onlineSdkEvents = backgroundScope.async {
                onlineEvents.take(1).toList()
            }

            onlineEvents.emit(event)

            advanceUntilIdle()

            onlineSdkEvents.await() shouldNotBe null
            verifySuspend {
                mockEmbeddedMessagesRequestFactory.create(
                    event
                )
            }
            verifySuspend { mockECNetworkClient.send(request) }
            verifySuspend { mockEventsDao.removeEvent(event) }
            verifySuspend {
                mockSdkEventManager.emitEvent(
                    SdkEvent.Internal.Sdk.Answer.Response(
                        event.id,
                        Result.success(expectedResponse)
                    )
                )
            }
            embeddedMessagingContext.metaData shouldBe expectedResponse.body<MetaData>()
        }

    private fun createMetaDataResponseString(): String {
        val metaDataResponseString = """{
  "version" : "v1",
  "labels" : {
    "allMessagesHeader" : "All Messages",
    "unreadMessagesHeader" : "Unread Messages",
    "filterCategories" : "Filter Categories",
    "pinnedMessagesTitle" : "Pinned Messages",
    "detailedMessageCloseButton" : "Close",
    "deleteDetailedMessageButton" : "Delete",
    "emptyStateTitle" : "No Messages",
    "emptyStateDescription" : "You have no messages at the moment."
  },
  "design" : {
    "fillColor" : {
      "primary" : "#FF526525",
      "onPrimary" : "#FFFFFFFF",
      "primaryContainer" : "#FFE8F5E8",
      "onPrimaryContainer" : "#FF1B5E20",
      "secondary" : "#FF2E7D32",
      "onSecondary" : "#FFFFFFFF",
      "secondaryContainer" : "#FFDFE6C5",
      "onSecondaryContainer" : "#FF181E09",
      "tertiary" : "#FF388E3C",
      "onTertiary" : "#FFFFFFFF",
      "tertiaryContainer" : "#FFA5D6A7",
      "onTertiaryContainer" : "#FF1B5E20",
      "error" : "#FFD32F2F",
      "onError" : "#FFFFFFFF",
      "errorContainer" : "#FFFFEBEE",
      "onErrorContainer" : "#FFD32F2F",
      "background" : "#FFF4F4E8",
      "onBackground" : "#FF1B5E20",
      "surface" : "#FFEFEFE2",
      "onSurface" : "#FF45483C",
      "surfaceVariant" : "#FFF6F5E9",
      "onSurfaceVariant" : "#FF2E7D32",
      "surfaceContainer" : "#FFF5F5F5",
      "surfaceContainerHigh" : "#FFE9E9DD",
      "surfaceContainerHighest" : "#FFE8F5E8",
      "surfaceContainerLow" : "#FFF4F4E8",
      "surfaceContainerLowest" : "#FFFFFFFF",
      "surfaceDim" : "#FFE0E0E0",
      "surfaceBright" : "#FFFFFFFF",
      "outline" : "#FF76786B",
      "outlineVariant" : "#FFA5D6A7",
      "inverseSurface" : "#FF2E7D32",
      "inverseOnSurface" : "#FFFFFFFF",
      "inversePrimary" : "#FF4CAF50",
      "scrim" : "#80000000",
      "surfaceTint": "#526525",
      "primaryFixed": "#ffffff",
      "primaryFixedDim": "#e4edcf",
      "onPrimaryFixed": "#000000",
      "onPrimaryFixedVariant": "#151a0a",
      "secondaryFixed": "#ffffff",
      "secondaryFixedDim": "#d7ead2",
      "onSecondaryFixed": "#000000",
      "onSecondaryFixedVariant": "#0e190b",
      "tertiaryFixed": "#ffffff",
      "tertiaryFixedDim": "#eae5d2",
      "onTertiaryFixed": "#000000",
      "onTertiaryFixedVariant": "#19160b"
    },
    "text" : {
      "displayLargeFontSize" : 57,
      "displayMediumFontSize" : 45,
      "displaySmallFontSize" : 36,
      "headlineLargeFontSize" : 32,
      "headlineMediumFontSize" : 28,
      "headlineSmallFontSize" : 24,
      "titleLargeFontSize" : 22,
      "titleMediumFontSize" : 16,
      "titleSmallFontSize" : 14,
      "bodyLargeFontSize" : 16,
      "bodyMediumFontSize" : 14,
      "bodySmallFontSize" : 12,
      "labelLargeFontSize" : 14,
      "labelMediumFontSize" : 12,
      "labelSmallFontSize" : 11
    },
    "misc" : {
        "messageItemMargin": 8,
        "messageItemElevation": 8,
        "buttonElevation": 8,
        "listContentPadding": 8,
        "listItemSpacing": 8,
        "compactOverlayWidth": 8,
        "compactOverlayMaxHeight": 8,
        "compactOverlayCornerRadius": 8,
        "compactOverlayElevation": 8,
        "messageItemCardCornerRadius": 8,
        "messageItemCardElevation": 8,
        "messageItemImageHeight": 8,
        "messageItemImageClipShape": "rectangle",
        "messageItemImageCornerRadius": 8
    }
  }
}"""
        return metaDataResponseString
    }
}
