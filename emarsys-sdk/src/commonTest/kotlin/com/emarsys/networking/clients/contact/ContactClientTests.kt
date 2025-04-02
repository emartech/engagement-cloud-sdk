package com.emarsys.networking.clients.contact

import com.emarsys.SdkConfig
import com.emarsys.context.SdkContextApi
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.session.SessionContext
import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.networking.clients.event.model.SdkEvent
import com.emarsys.util.JsonUtil
import dev.mokkery.MockMode
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
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
import io.kotest.matchers.shouldBe
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
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
    private lateinit var sessionContext: SessionContext
    private lateinit var json: Json
    private lateinit var onlineEvents: MutableSharedFlow<SdkEvent>
    private lateinit var sdkEventDistributor: SdkEventDistributorApi
    private lateinit var sdkDispatcher: CoroutineDispatcher
    private lateinit var contactClient: ContactClient

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        mockEmarsysClient = mock()
        mockUrlFactory = mock()
        mockSdkContext = mock()
        mockConfig = mock()
        mockContactTokenHandler = mock()
        mockLogger = mock(MockMode.autofill)
        sessionContext = SessionContext(refreshToken = "testRefreshToken")
        json = JsonUtil.json
        onlineEvents = MutableSharedFlow(replay = 5)
        sdkEventDistributor = mock()
        everySuspend { sdkEventDistributor.onlineEvents } returns onlineEvents
        sdkDispatcher = StandardTestDispatcher()
        every { mockSdkContext.config } returns mockConfig
        everySuspend { mockContactTokenHandler.handleContactTokens(any()) } returns Unit
        everySuspend { mockEmarsysClient.send(any()) } returns (createTestResponse("{}"))
        every { mockConfig.merchantId } returns null
        every { mockSdkContext.contactFieldId = any() } returns Unit
        every { mockUrlFactory.create(EmarsysUrlType.LINK_CONTACT, null) } returns TEST_BASE_URL
        every { mockUrlFactory.create(EmarsysUrlType.UNLINK_CONTACT, null) } returns TEST_BASE_URL
        everySuspend { mockLogger.error(any(), any<Throwable>()) } calls {
            (it.args[1] as Throwable).printStackTrace()
            throw it.args[1] as Throwable
        }

        contactClient = ContactClient(
            mockEmarsysClient,
            sdkEventDistributor,
            mockUrlFactory,
            mockSdkContext,
            mockContactTokenHandler,
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
    fun testConsumer_should_call_client_with_linkContact_request() = runTest {
        contactClient.register()

        val linkContact = SdkEvent.Internal.Sdk.LinkContact(
            "linkContact",
            attributes = buildJsonObject {
                put("contactFieldId", JsonPrimitive(CONTACT_FIELD_ID))
                put("contactFieldValue", JsonPrimitive(CONTACT_FIELD_VALUE))
            })

        onlineEvents.emit(linkContact)

        advanceUntilIdle()

        verify { mockUrlFactory.create(any()) }
        verifySuspend { mockEmarsysClient.send(any()) }
        verifySuspend { mockContactTokenHandler.handleContactTokens(any()) }
        verifySuspend { mockSdkContext.contactFieldId = CONTACT_FIELD_ID }
    }

    @Test
    fun testConsumer_should_not_call_contactTokenHandler_when_client_responds_with_204() = runTest {
        contactClient.register()

        val requestSlot = slot<UrlRequest>()
        everySuspend { mockEmarsysClient.send(capture(requestSlot)) }.returns(
            createTestResponse(
                "{}",
                HttpStatusCode.NoContent
            )
        )
        every { mockConfig.merchantId } returns MERCHANT_ID
        val linkContact = SdkEvent.Internal.Sdk.LinkContact(
            "linkContact",
            attributes = buildJsonObject {
                put("contactFieldId", JsonPrimitive(CONTACT_FIELD_ID))
                put("contactFieldValue", JsonPrimitive(CONTACT_FIELD_VALUE))
            })

        onlineEvents.emit(linkContact)

        advanceUntilIdle()

        verify { mockUrlFactory.create(any()) }
        verifySuspend { mockEmarsysClient.send(any()) }
        verifySuspend(VerifyMode.exactly(0)) { mockContactTokenHandler.handleContactTokens(any()) }
        verifySuspend { mockSdkContext.contactFieldId = CONTACT_FIELD_ID }

        val request = requestSlot.get()
        request.headers?.containsValue(MERCHANT_ID) shouldBe true
    }

    @Test
    fun testConsumer_should_call_client_with_linkAuthenticatedContact_request() = runTest {
        contactClient.register()

        val requestSlot = slot<UrlRequest>()
        everySuspend { mockEmarsysClient.send(capture(requestSlot)) }.returns(createTestResponse("{}"))
        every { mockConfig.merchantId } returns MERCHANT_ID
        val linkAuthenticatedContact = SdkEvent.Internal.Sdk.LinkAuthenticatedContact(
            "linkAuthenticatedContact",
            attributes = buildJsonObject {
                put("contactFieldId", JsonPrimitive(CONTACT_FIELD_ID))
                put("openIdToken", JsonPrimitive(OPEN_ID_TOKEN))
            })

        onlineEvents.emit(linkAuthenticatedContact)

        advanceUntilIdle()

        verify { mockUrlFactory.create(any()) }
        verifySuspend { mockEmarsysClient.send(any()) }
        verifySuspend { mockContactTokenHandler.handleContactTokens(any()) }
        verifySuspend { mockSdkContext.contactFieldId = CONTACT_FIELD_ID }

        val request = requestSlot.get()
        request.headers?.containsValue(MERCHANT_ID) shouldBe true
    }

    @Test
    fun testConsumer_should_call_client_with_unlinkContact_request() = runTest {
        contactClient.register()

        val unlinkContact = SdkEvent.Internal.Sdk.UnlinkContact("unlinkContact")

        onlineEvents.emit(unlinkContact)

        advanceUntilIdle()

        verify { mockUrlFactory.create(any()) }
        verifySuspend { mockEmarsysClient.send(any()) }
        verifySuspend { mockContactTokenHandler.handleContactTokens(any()) }
        verifySuspend { mockSdkContext.contactFieldId = null }
    }

    @Test
    fun testConsumer_not_should_call_handleTokens_and_should_not_store_contactFieldId_if_request_fails() =
        runTest {
            contactClient.register()

            everySuspend { mockEmarsysClient.send(any()) } returns createTestResponse(statusCode = HttpStatusCode.BadRequest)
            val unlinkContact = SdkEvent.Internal.Sdk.UnlinkContact("unlinkContact")

            onlineEvents.emit(unlinkContact)

            advanceUntilIdle()

            verify { mockUrlFactory.create(any()) }
            verifySuspend { mockEmarsysClient.send(any()) }

            backgroundScope.launch {
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