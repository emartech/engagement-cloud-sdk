package com.sap.ec.core.channel

import com.sap.ec.context.DefaultUrlsApi
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.url.ECUrlType
import com.sap.ec.core.url.UrlFactory
import com.sap.ec.core.url.UrlFactoryApi
import com.sap.ec.event.SdkEvent.Internal.OperationalEvent
import com.sap.ec.event.SdkEvent.Internal.Sdk.ApplyGlobalRemoteConfig
import com.sap.ec.event.SdkEvent.Internal.Sdk.ClearPushToken
import com.sap.ec.event.SdkEvent.Internal.Sdk.UnlinkContact
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture.Companion.slot
import dev.mokkery.matcher.capture.SlotCapture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
import dev.mokkery.spy
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class OperationalEventDistributorTests {
    private companion object {
        const val APP_CODE = "INT-ABCDE-12345"
        const val CLIENT_SERVICE_URL = "testClientServiceUrl"
        val testEvent = ClearPushToken(applicationCode = APP_CODE)
    }

    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi
    private lateinit var urlFactory: UrlFactoryApi
    private lateinit var urlFactorySpy: UrlFactoryApi
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var mockDefaultUrls: DefaultUrlsApi
    private lateinit var eventSlot: SlotCapture<OperationalEvent>
    private lateinit var mockEventWaiter: SdkEventWaiterApi
    private lateinit var operationalEventDistributor: OperationalEventDistributorApi

    @BeforeTest
    fun setup() {
        eventSlot = slot()
        mockEventWaiter = mock(MockMode.autofill)
        mockSdkEventDistributor = mock(MockMode.autofill)
        everySuspend {
            mockSdkEventDistributor.registerEvent(capture(eventSlot))
        } returns mockEventWaiter
        mockDefaultUrls = mock(MockMode.autofill)
        every { mockDefaultUrls.clientServiceBaseUrl } returns CLIENT_SERVICE_URL
        mockSdkContext = mock(MockMode.autofill)
        every { mockSdkContext.defaultUrls } returns mockDefaultUrls
        urlFactory = UrlFactory(mockSdkContext)
        urlFactorySpy = spy(urlFactory)
        operationalEventDistributor =
            OperationalEventDistributor(mockSdkEventDistributor, urlFactorySpy)
    }

    @Test
    fun registerOperationalEvent_shouldCallRegisterEvent_onSdkEventDistributor_withAddedUrl() =
        runTest {
            val expectedUrl = "$CLIENT_SERVICE_URL/v4/apps/$APP_CODE/client/push-token"

            operationalEventDistributor.registerEvent(testEvent)

            verifySuspend { mockSdkEventDistributor.registerEvent(any()) }
            eventSlot.get().targetUrl.toString().endsWith(expectedUrl) shouldBe true
        }

    @Test
    fun registerEvent_shouldReturn_eventWaiter_fromSdkEventDistributor() =
        runTest {
            val result = operationalEventDistributor.registerEvent(testEvent)

            result shouldBe mockEventWaiter
        }

    @Test
    fun registerOperationalEvent_shouldCallRegisterEvent_onSdkEventDistributor_withOriginalEvent_ifApplicationCode_isNull() =
        runTest {
            val unlinkContactEvent = UnlinkContact(applicationCode = null)
            operationalEventDistributor.registerEvent(unlinkContactEvent)

            verifySuspend {
                mockSdkEventDistributor.registerEvent(unlinkContactEvent)
            }
        }

    @Test
    fun registerEvent_shouldCallCreate_onUrlFactory() = runTest {
        operationalEventDistributor.registerEvent(testEvent)

        verifySuspend { urlFactorySpy.create(ECUrlType.ClearPushToken(APP_CODE)) }
    }

    @Test
    fun registerEvent_shouldNotCallCreate_onUrlFactory_ifApplicationCode_isNull() =
        runTest {
            operationalEventDistributor.registerEvent(ApplyGlobalRemoteConfig())

            verifySuspend(VerifyMode.exactly(0)) {
                urlFactorySpy.create(ECUrlType.ClearPushToken(APP_CODE))
            }
        }
}