package com.emarsys.networking.clients.contact

import com.emarsys.SdkConfig
import com.emarsys.context.SdkContextApi
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.session.SessionContext
import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.di.DispatcherTypes
import com.emarsys.di.EventFlowTypes
import com.emarsys.di.NetworkClientTypes
import com.emarsys.di.SdkKoinIsolationContext.koin
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
import dev.mokkery.spy
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
import org.koin.core.Koin
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.test.KoinTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ContactClientTests : KoinTest {
    override fun getKoin(): Koin = koin

    init {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    private companion object {
        val TEST_BASE_URL = Url("https://test-base-url/")
        const val OPEN_ID_TOKEN = "testOpenIdToken"
        const val CONTACT_FIELD_VALUE = "testContactFieldValue"
        const val CONTACT_FIELD_ID = 2575
        const val MERCHANT_ID = "testMerchantId"
        val mockEmarsysClient: NetworkClientApi = mock()
        val mockUrlFactory: UrlFactoryApi = mock()
        val mockSdkContext: SdkContextApi = mock()
        val mockConfig: SdkConfig = mock()
        val mockContactTokenHandler: ContactTokenHandlerApi = mock()
        val mockLogger: Logger = mock(MockMode.autofill)
        val sessionContext = SessionContext(refreshToken = "testRefreshToken")
        val json: Json = JsonUtil.json
        val sdkEventFlow: MutableSharedFlow<SdkEvent> = spy(MutableSharedFlow(replay = 5))
    }

    private lateinit var sdkDispatcher: CoroutineDispatcher
    private lateinit var testModules: Module

    private fun createContactClientTestModules() {
        testModules = module {
            single<NetworkClientApi>(named(NetworkClientTypes.Emarsys)) { mockEmarsysClient }
            single<UrlFactoryApi> { mockUrlFactory }
            single<ContactTokenHandlerApi> { mockContactTokenHandler }
            single<SdkContextApi> { mockSdkContext }
            single<Logger> { mockLogger }
            single<SdkConfig> { mockConfig }
            single<Json> { json }
            single<SessionContext> { sessionContext }
            single<CoroutineDispatcher>(named(DispatcherTypes.Sdk)) { sdkDispatcher }
            single<MutableSharedFlow<SdkEvent>>(named(EventFlowTypes.InternalEventFlow)) { sdkEventFlow }
        }
        koin.loadModules(listOf(testModules))
    }


    @BeforeTest
    fun setUp() {
        sdkDispatcher = StandardTestDispatcher()
        every { mockSdkContext.config } returns mockConfig
        everySuspend { mockContactTokenHandler.handleContactTokens(any()) } returns Unit
        everySuspend { mockEmarsysClient.send(any()) }.returns(createTestResponse("{}"))
        every { mockConfig.merchantId } returns null
        every { mockSdkContext.contactFieldId = any() } returns Unit
        every { mockUrlFactory.create(EmarsysUrlType.LINK_CONTACT, null) } returns TEST_BASE_URL
        every { mockUrlFactory.create(EmarsysUrlType.UNLINK_CONTACT, null) } returns TEST_BASE_URL
        everySuspend { mockLogger.error(any(), any<Throwable>()) } calls {
            (it.args[1] as Throwable).printStackTrace()
            throw it.args[1] as Throwable
        }
        createContactClientTestModules()
        ContactClient
    }

    @AfterTest
    fun tearDown() {
        resetCalls()
        resetAnswers()
        koin.unloadModules(listOf(testModules))
    }

    @Test
    fun testConsumer_should_call_client_with_linkContact_request() = runTest {
        val linkContact = SdkEvent.Internal.Sdk.LinkContact(
            "linkContact",
            attributes = buildJsonObject {
                put("contactFieldId", JsonPrimitive(CONTACT_FIELD_ID))
                put("contactFieldValue", JsonPrimitive(CONTACT_FIELD_VALUE))
            })

        sdkEventFlow.emit(linkContact)

        advanceUntilIdle()

        verify { mockUrlFactory.create(any()) }
        verifySuspend { mockEmarsysClient.send(any()) }
        verifySuspend { mockContactTokenHandler.handleContactTokens(any()) }
        verifySuspend { mockSdkContext.contactFieldId = CONTACT_FIELD_ID }
    }

    @Test
    fun testConsumer_should_call_client_with_linkAuthenticatedContact_request() = runTest {
        val requestSlot = slot<UrlRequest>()
        everySuspend { mockEmarsysClient.send(capture(requestSlot)) }.returns(createTestResponse("{}"))
        every { mockConfig.merchantId } returns MERCHANT_ID
        val linkAuthenticatedContact = SdkEvent.Internal.Sdk.LinkAuthenticatedContact(
            "linkAuthenticatedContact",
            attributes = buildJsonObject {
                put("contactFieldId", JsonPrimitive(CONTACT_FIELD_ID))
                put("openIdToken", JsonPrimitive(OPEN_ID_TOKEN))
            })

        sdkEventFlow.emit(linkAuthenticatedContact)

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
        val unlinkContact = SdkEvent.Internal.Sdk.UnlinkContact("unlinkContact")

        sdkEventFlow.emit(unlinkContact)

        advanceUntilIdle()

        verify { mockUrlFactory.create(any()) }
        verifySuspend { mockEmarsysClient.send(any()) }
        verifySuspend { mockContactTokenHandler.handleContactTokens(any()) }
        verifySuspend { mockSdkContext.contactFieldId = null }
    }

    @Test
    fun testConsumer_not_should_call_handleTokens_and_should_not_store_contactFieldId_if_request_fails() =
        runTest {
            everySuspend { mockEmarsysClient.send(any()) } returns createTestResponse(statusCode = HttpStatusCode.BadRequest)
            val unlinkContact = SdkEvent.Internal.Sdk.UnlinkContact("unlinkContact")

            sdkEventFlow.emit(unlinkContact)

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