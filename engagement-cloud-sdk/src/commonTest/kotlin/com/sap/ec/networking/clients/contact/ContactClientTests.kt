package com.sap.ec.networking.clients.contact

import com.sap.ec.config.SdkConfig
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.channel.SdkEventManagerApi
import com.sap.ec.core.db.events.EventsDaoApi
import com.sap.ec.core.exceptions.SdkException.NetworkIOException
import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.clients.NetworkClientApi
import com.sap.ec.core.networking.context.RequestContextApi
import com.sap.ec.core.networking.model.Response
import com.sap.ec.core.networking.model.UrlRequest
import com.sap.ec.core.url.ECUrlType
import com.sap.ec.core.url.UrlFactoryApi
import com.sap.ec.event.OnlineSdkEvent
import com.sap.ec.event.SdkEvent
import com.sap.ec.mobileengage.session.SessionApi
import com.sap.ec.networking.clients.error.ClientExceptionHandler
import com.sap.ec.util.JsonUtil
import dev.mokkery.MockMode
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.resetAnswers
import dev.mokkery.resetCalls
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class ContactClientTests {
    private companion object {
        val TEST_BASE_URL = Url("https://test-base-url/")
        const val OPEN_ID_TOKEN = "testOpenIdToken"
        const val CONTACT_FIELD_VALUE = "testContactFieldValue"
    }

    private lateinit var mockEcClient: NetworkClientApi
    private lateinit var mockUrlFactory: UrlFactoryApi
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var mockConfig: SdkConfig
    private lateinit var mockContactTokenHandler: ContactTokenHandlerApi
    private lateinit var mockLogger: Logger
    private lateinit var mockRequestContext: RequestContextApi
    private lateinit var mockEcSdkSession: SessionApi
    private lateinit var mockClientExceptionHandler: ClientExceptionHandler
    private lateinit var json: Json
    private lateinit var onlineEvents: MutableSharedFlow<OnlineSdkEvent>
    private lateinit var mockSdkEventManager: SdkEventManagerApi
    private lateinit var mockEventsDao: EventsDaoApi
    private lateinit var sdkDispatcher: CoroutineDispatcher
    private lateinit var contactClient: ContactClient

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        mockEcClient = mock()
        mockUrlFactory = mock()
        mockSdkContext = mock(mode = MockMode.autofill)
        mockConfig = mock()
        mockContactTokenHandler = mock()
        mockLogger = mock(MockMode.autofill)
        mockRequestContext = mock(MockMode.autofill)
        mockEcSdkSession = mock(MockMode.autofill)
        mockClientExceptionHandler = mock(MockMode.autofill)
        every { mockRequestContext.refreshToken } returns "testRefreshToken"
        json = JsonUtil.json
        onlineEvents = MutableSharedFlow(replay = 5)
        mockSdkEventManager = mock()
        mockEventsDao = mock(MockMode.autofill)
        everySuspend { mockSdkEventManager.onlineSdkEvents } returns onlineEvents
        everySuspend { mockSdkEventManager.emitEvent(any()) } returns Unit
        sdkDispatcher = StandardTestDispatcher()
        every { mockSdkContext.config } returns mockConfig
        everySuspend { mockContactTokenHandler.handleContactTokens(any()) } returns Unit
        everySuspend { mockEcClient.send(any()) } returns (Result.success(createTestResponse("{}")))
        every { mockUrlFactory.create(ECUrlType.LinkContact) } returns TEST_BASE_URL
        every { mockUrlFactory.create(ECUrlType.UnlinkContact) } returns TEST_BASE_URL
        everySuspend { mockLogger.error(any(), any<Throwable>()) } calls {
            (it.args[1] as Throwable).printStackTrace()
        }

        contactClient = ContactClient(
            mockEcClient,
            mockClientExceptionHandler,
            mockSdkEventManager,
            mockUrlFactory,
            mockSdkContext,
            mockContactTokenHandler,
            mockEcSdkSession,
            mockEventsDao,
            json,
            mockLogger,
            sdkDispatcher
        )
    }

    @AfterTest
    fun tearDown() {
        resetCalls()
        resetAnswers()
    }

    @Test
    fun testConsumer_should_call_client_with_linkContact_request_andAckEvent() = runTest {
        contactClient.register()

        val linkContactEvent = SdkEvent.Internal.Sdk.LinkContact(
            "linkContact",
            contactFieldValue = CONTACT_FIELD_VALUE
        )

        onlineEvents.emit(linkContactEvent)

        advanceUntilIdle()

        verify { mockUrlFactory.create(any()) }
        verifySuspend { mockEcClient.send(any()) }
        verifySuspend { mockContactTokenHandler.handleContactTokens(any()) }
        verifySuspend { mockSdkContext.contactFieldValue = CONTACT_FIELD_VALUE }
        verifySuspend { mockEcSdkSession.startSession() }
        verifySuspend { mockEventsDao.removeEvent(linkContactEvent) }

    }

    @Test
    fun testConsumer_should_not_call_contactTokenHandler_when_client_responds_with_204() = runTest {
        contactClient.register()

        everySuspend { mockEcClient.send(any()) }.returns(
            Result.success(
                createTestResponse(
                    "{}",
                    HttpStatusCode.NoContent
                )
            )
        )
        val linkContactEvent = SdkEvent.Internal.Sdk.LinkContact(
            "linkContact",
            contactFieldValue = CONTACT_FIELD_VALUE
        )

        onlineEvents.emit(linkContactEvent)

        advanceUntilIdle()

        verify { mockUrlFactory.create(any()) }
        verifySuspend { mockEcClient.send(any()) }
        verifySuspend(VerifyMode.exactly(0)) { mockContactTokenHandler.handleContactTokens(any()) }
        verifySuspend { mockEcSdkSession.startSession() }
        verifySuspend { mockEventsDao.removeEvent(linkContactEvent) }
    }

    @Test
    fun testConsumer_should_call_client_with_linkAuthenticatedContact_request() = runTest {
        contactClient.register()

        everySuspend { mockEcClient.send(any()) }.returns(
            Result.success(createTestResponse("{}"))
        )
        val linkAuthenticatedContactEvent = SdkEvent.Internal.Sdk.LinkAuthenticatedContact(
            "linkAuthenticatedContact",
            openIdToken = OPEN_ID_TOKEN
        )

        onlineEvents.emit(linkAuthenticatedContactEvent)

        advanceUntilIdle()

        verify { mockUrlFactory.create(any()) }
        verifySuspend { mockEcClient.send(any()) }
        verifySuspend { mockContactTokenHandler.handleContactTokens(any()) }
        verifySuspend { mockSdkContext.openIdToken = OPEN_ID_TOKEN }
        verifySuspend { mockEcSdkSession.startSession() }
        verifySuspend { mockEventsDao.removeEvent(linkAuthenticatedContactEvent) }
    }

    @Test
    fun testConsumer_should_call_client_with_unlinkContact_request() = runTest {
        contactClient.register()

        val unlinkContactEvent = SdkEvent.Internal.Sdk.UnlinkContact("unlinkContact")

        onlineEvents.emit(unlinkContactEvent)

        advanceUntilIdle()

        verify { mockUrlFactory.create(any()) }
        verifySuspend { mockEcClient.send(any()) }
        verifySuspend { mockContactTokenHandler.handleContactTokens(any()) }
        verifySuspend { mockSdkContext.contactFieldValue = null }
        verifySuspend { mockSdkContext.openIdToken = null }
        verifySuspend { mockEcSdkSession.endSession() }
        verifySuspend { mockEventsDao.removeEvent(unlinkContactEvent) }
    }

    @Test
    fun testConsumer_should_reEmit_events_into_flow_when_there_is_a_network_error() = runTest {
        contactClient.register()

        val unlinkContactEvent = SdkEvent.Internal.Sdk.UnlinkContact("unlinkContact")
        val testException = NetworkIOException("No Internet")
        everySuspend { mockEcClient.send(any()) } returns Result.failure(testException)
        everySuspend { mockSdkEventManager.emitEvent(any()) } returns Unit


        onlineEvents.emit(unlinkContactEvent)

        advanceUntilIdle()

        verify { mockUrlFactory.create(any()) }
        verifySuspend { mockEcClient.send(any()) }
        verifySuspend(VerifyMode.exactly(0)) { mockContactTokenHandler.handleContactTokens(any()) }
        verifySuspend {
            mockClientExceptionHandler.handleException(
                testException,
                any(),
                unlinkContactEvent
            )
        }
        verifySuspend {
            mockSdkEventManager.emitEvent(
                unlinkContactEvent
            )
        }
    }

    @Test
    fun testConsumer_not_should_call_handleTokens_and_use_clientExceptionHandler_on_exception() =
        runTest {
            contactClient.register()
            val testException = Exception("Test exception")

            everySuspend { mockEcClient.send(any()) } throws testException
            everySuspend { mockSdkEventManager.emitEvent(any()) } returns Unit

            val unlinkContact = SdkEvent.Internal.Sdk.UnlinkContact("unlinkContact")

            onlineEvents.emit(unlinkContact)

            advanceUntilIdle()

            verify { mockUrlFactory.create(any()) }
            verifySuspend { mockEcClient.send(any()) }
            verifySuspend {
                mockClientExceptionHandler.handleException(
                    testException,
                    any(),
                    unlinkContact
                )
            }
            verifySuspend(VerifyMode.exactly(0)) {
                mockContactTokenHandler.handleContactTokens(any())
            }
      
        }


    private fun createTestResponse(
        body: String = "{}",
        statusCode: HttpStatusCode = HttpStatusCode.OK
    ) = Response(
        UrlRequest(
            TEST_BASE_URL,
            HttpMethod.Post
        ), statusCode, Headers.Empty, bodyAsText = body
    )
}