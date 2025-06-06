package com.emarsys.networking.clients.contact

import com.emarsys.config.SdkConfig
import com.emarsys.context.SdkContextApi
import com.emarsys.core.channel.SdkEventManagerApi
import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.exceptions.FailedRequestException
import com.emarsys.core.exceptions.RetryLimitReachedException
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.context.RequestContextApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.mobileengage.session.SessionApi
import com.emarsys.networking.clients.event.model.OnlineSdkEvent
import com.emarsys.networking.clients.event.model.SdkEvent
import com.emarsys.util.JsonUtil
import dev.mokkery.MockMode
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture.Companion.slot
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
import dev.mokkery.resetAnswers
import dev.mokkery.resetCalls
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.matchers.shouldBe
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
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ContactClientTests {
    private companion object {
        val TEST_BASE_URL = Url("https://test-base-url/")
        const val OPEN_ID_TOKEN = "testOpenIdToken"
        const val CONTACT_FIELD_VALUE = "testContactFieldValue"
        const val CONTACT_FIELD_ID = 2575
        const val MERCHANT_ID = "testMerchantId"
    }

    private lateinit var mockEmarsysClient: NetworkClientApi
    private lateinit var mockUrlFactory: UrlFactoryApi
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var mockConfig: SdkConfig
    private lateinit var mockContactTokenHandler: ContactTokenHandlerApi
    private lateinit var mockLogger: Logger
    private lateinit var mockRequestContext: RequestContextApi
    private lateinit var mockEmarsysSdkSession: SessionApi
    private lateinit var json: Json
    private lateinit var onlineEvents: MutableSharedFlow<OnlineSdkEvent>
    private lateinit var mockSdkEventManager: SdkEventManagerApi
    private lateinit var mockEventsDao: EventsDaoApi
    private lateinit var sdkDispatcher: CoroutineDispatcher
    private lateinit var contactClient: ContactClient

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        mockEmarsysClient = mock()
        mockUrlFactory = mock()
        mockSdkContext = mock(mode = MockMode.autofill)
        mockConfig = mock()
        mockContactTokenHandler = mock()
        mockLogger = mock(MockMode.autofill)
        mockRequestContext = mock(MockMode.autofill)
        mockEmarsysSdkSession = mock(MockMode.autofill)
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
        everySuspend { mockEmarsysClient.send(any(), any()) } returns (createTestResponse("{}"))
        every { mockConfig.merchantId } returns null
        every { mockSdkContext.contactFieldId = any() } returns Unit
        every { mockUrlFactory.create(EmarsysUrlType.LINK_CONTACT) } returns TEST_BASE_URL
        every { mockUrlFactory.create(EmarsysUrlType.UNLINK_CONTACT) } returns TEST_BASE_URL
        everySuspend { mockLogger.error(any(), any<Throwable>()) } calls {
            (it.args[1] as Throwable).printStackTrace()
        }

        contactClient = ContactClient(
            mockEmarsysClient,
            mockSdkEventManager,
            mockUrlFactory,
            mockSdkContext,
            mockContactTokenHandler,
            mockEmarsysSdkSession,
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
            contactFieldId = CONTACT_FIELD_ID,
            contactFieldValue = CONTACT_FIELD_VALUE
        )

        onlineEvents.emit(linkContactEvent)

        advanceUntilIdle()

        verify { mockUrlFactory.create(any()) }
        verifySuspend { mockEmarsysClient.send(any(), any()) }
        verifySuspend { mockContactTokenHandler.handleContactTokens(any()) }
        verifySuspend { mockSdkContext.contactFieldId = CONTACT_FIELD_ID }
        verifySuspend { mockSdkContext.contactFieldValue = CONTACT_FIELD_VALUE }
        verifySuspend { mockEmarsysSdkSession.startSession() }
        verifySuspend { mockEventsDao.removeEvent(linkContactEvent) }

    }

    @Test
    fun testConsumer_should_not_call_contactTokenHandler_when_client_responds_with_204() = runTest {
        contactClient.register()

        val requestSlot = slot<UrlRequest>()
        everySuspend { mockEmarsysClient.send(capture(requestSlot), any()) }.returns(
            createTestResponse(
                "{}",
                HttpStatusCode.NoContent
            )
        )
        every { mockConfig.merchantId } returns MERCHANT_ID
        val linkContactEvent = SdkEvent.Internal.Sdk.LinkContact(
            "linkContact",
            contactFieldId = CONTACT_FIELD_ID,
            contactFieldValue = CONTACT_FIELD_VALUE
        )

        onlineEvents.emit(linkContactEvent)

        advanceUntilIdle()

        verify { mockUrlFactory.create(any()) }
        verifySuspend { mockEmarsysClient.send(any(), any()) }
        verifySuspend(VerifyMode.exactly(0)) { mockContactTokenHandler.handleContactTokens(any()) }
        verifySuspend { mockSdkContext.contactFieldId = CONTACT_FIELD_ID }
        verifySuspend { mockEmarsysSdkSession.startSession() }
        verifySuspend { mockEventsDao.removeEvent(linkContactEvent) }

        val request = requestSlot.get()
        request.headers?.containsValue(MERCHANT_ID) shouldBe true
    }

    @Test
    fun testConsumer_should_call_client_with_linkAuthenticatedContact_request() = runTest {
        contactClient.register()

        val requestSlot = slot<UrlRequest>()
        everySuspend { mockEmarsysClient.send(capture(requestSlot), any()) }.returns(
            createTestResponse("{}")
        )
        every { mockConfig.merchantId } returns MERCHANT_ID
        val linkAuthenticatedContactEvent = SdkEvent.Internal.Sdk.LinkAuthenticatedContact(
            "linkAuthenticatedContact",
            contactFieldId = CONTACT_FIELD_ID,
            openIdToken = OPEN_ID_TOKEN
        )

        onlineEvents.emit(linkAuthenticatedContactEvent)

        advanceUntilIdle()

        verify { mockUrlFactory.create(any()) }
        verifySuspend { mockEmarsysClient.send(any(), any()) }
        verifySuspend { mockContactTokenHandler.handleContactTokens(any()) }
        verifySuspend { mockSdkContext.contactFieldId = CONTACT_FIELD_ID }
        verifySuspend { mockSdkContext.openIdToken = OPEN_ID_TOKEN }
        verifySuspend { mockEmarsysSdkSession.startSession() }
        verifySuspend { mockEventsDao.removeEvent(linkAuthenticatedContactEvent) }

        val request = requestSlot.get()
        request.headers?.containsValue(MERCHANT_ID) shouldBe true
    }

    @Test
    fun testConsumer_should_not_fail_flow_collection_when_unhandled_exception_is_thrown() =
        runTest {
            contactClient.register()

            everySuspend {
                mockEmarsysClient.send(
                    any(),
                    any()
                )
            } throws Exception("Unhandled exception")
            val linkAuthenticatedContactEvent = SdkEvent.Internal.Sdk.LinkAuthenticatedContact(
                "linkAuthenticatedContact",
                contactFieldId = CONTACT_FIELD_ID,
                openIdToken = OPEN_ID_TOKEN
            )

            onlineEvents.emit(linkAuthenticatedContactEvent)

            advanceUntilIdle()

            verify { mockUrlFactory.create(any()) }
            verifySuspend { mockEmarsysClient.send(any(), any()) }
            verifySuspend(VerifyMode.exactly(0)) { mockContactTokenHandler.handleContactTokens(any()) }
            verifySuspend(VerifyMode.exactly(0)) {
                mockSdkContext.contactFieldId = CONTACT_FIELD_ID
            }
            verifySuspend(VerifyMode.exactly(0)) { mockEmarsysSdkSession.startSession() }
            verifySuspend(VerifyMode.exactly(0)) {
                mockEventsDao.removeEvent(linkAuthenticatedContactEvent)
            }
        }

    @Test
    fun testConsumer_should_call_client_with_unlinkContact_request() = runTest {
        contactClient.register()

        val unlinkContactEvent = SdkEvent.Internal.Sdk.UnlinkContact("unlinkContact")

        onlineEvents.emit(unlinkContactEvent)

        advanceUntilIdle()

        verify { mockUrlFactory.create(any()) }
        verifySuspend { mockEmarsysClient.send(any(), any()) }
        verifySuspend { mockContactTokenHandler.handleContactTokens(any()) }
        verifySuspend { mockSdkContext.contactFieldId = null }
        verifySuspend { mockSdkContext.contactFieldValue = null }
        verifySuspend { mockSdkContext.openIdToken = null }
        verifySuspend { mockEmarsysSdkSession.endSession() }
        verifySuspend { mockEventsDao.removeEvent(unlinkContactEvent) }
    }

    @Test
    fun testConsumer_should_reEmit_events_into_flow_when_there_is_a_network_error() = runTest {
        contactClient.register()

        val unlinkContactEvent = SdkEvent.Internal.Sdk.UnlinkContact("unlinkContact")
        everySuspend { mockEmarsysClient.send(any(), any()) } calls { args ->
            (args.arg(1) as suspend () -> Unit).invoke()
            throw IOException("No Internet")
        }

        onlineEvents.emit(unlinkContactEvent)

        advanceUntilIdle()

        verify { mockUrlFactory.create(any()) }
        verifySuspend { mockEmarsysClient.send(any(), any()) }
        verifySuspend(VerifyMode.exactly(0)) { mockContactTokenHandler.handleContactTokens(any()) }
        verifySuspend(VerifyMode.exactly(0)) { mockEventsDao.removeEvent(unlinkContactEvent) }
        verifySuspend { mockSdkEventManager.emitEvent(unlinkContactEvent) }
    }

    @Test
    fun testConsumer_not_should_call_handleTokens_and_should_not_store_contactFieldId_and_should_ack_event_if_request_fails_or_retries_are_exhausted() =
        forAll(
            table(
                headers("exception"),
                listOf(
                    row(
                        FailedRequestException(
                            createTestResponse(
                                statusCode = HttpStatusCode.BadRequest
                            )
                        )
                    ),
                    row(RetryLimitReachedException("Retry limit reached"))
                )
            )
        ) { testException ->
            runTest {
                contactClient.register()

                everySuspend { mockEmarsysClient.send(any(), any()) } throws testException
                val unlinkContact = SdkEvent.Internal.Sdk.UnlinkContact("unlinkContact")

                onlineEvents.emit(unlinkContact)

                advanceUntilIdle()

                verify { mockUrlFactory.create(any()) }
                verifySuspend { mockEmarsysClient.send(any(), any()) }
                verifySuspend { mockEventsDao.removeEvent(unlinkContact) }

                verifySuspend(VerifyMode.exactly(0)) {
                    mockContactTokenHandler.handleContactTokens(any())
                }
                verifySuspend(VerifyMode.exactly(0)) { mockSdkContext.contactFieldId = null }
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