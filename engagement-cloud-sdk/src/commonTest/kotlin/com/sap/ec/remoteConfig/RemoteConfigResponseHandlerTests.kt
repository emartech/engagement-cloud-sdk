package com.sap.ec.remoteConfig

import com.sap.ec.context.DefaultUrls
import com.sap.ec.context.DefaultUrlsApi
import com.sap.ec.context.Features
import com.sap.ec.context.Features.EmbeddedMessaging
import com.sap.ec.context.Features.JsBridgeSignatureCheck
import com.sap.ec.context.Features.MobileEngage
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.device.DeviceInfoCollectorApi
import com.sap.ec.core.log.LogLevel
import com.sap.ec.core.log.SdkLogger
import com.sap.ec.core.providers.DoubleProvider
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.capture.Capture.Companion.slot
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
import dev.mokkery.verify
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class RemoteConfigResponseHandlerTests {
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var defaultUrls: DefaultUrlsApi
    private lateinit var mockDeviceInfoCollector: DeviceInfoCollectorApi
    private lateinit var mockRandomProvider: DoubleProvider
    private lateinit var remoteConfigResponseHandler: RemoteConfigResponseHandler

    @BeforeTest
    fun setUp() {
        mockSdkContext = mock(MockMode.autofill)
        defaultUrls = DefaultUrls("", "", "", "", "", "", "", "")
        every { mockSdkContext.defaultUrls } returns defaultUrls

        everySuspend { mockSdkContext.sdkDispatcher } returns StandardTestDispatcher()
        every { mockSdkContext.features } returns mutableSetOf(JsBridgeSignatureCheck)

        mockDeviceInfoCollector = mock(MockMode.autofill)
        mockRandomProvider = mock(MockMode.autofill)

        remoteConfigResponseHandler = RemoteConfigResponseHandler(
            mockDeviceInfoCollector,
            mockSdkContext,
            mockRandomProvider,
            SdkLogger("TestLoggerName", mock(MockMode.autofill), sdkContext = mockSdkContext)
        )
    }

    @Test
    fun testHandleAppCodeBasedConfigs() = runTest {
        val defaultUrlSlot = slot<DefaultUrlsApi>()
        val clientServiceUrl = "testClientServiceUrl"
        val eventServiceUrl = "testEventServiceUrl"
        val deeplinkServiceUrl = "testDeepLinkServiceUrl"
        val jsBridgeUrl = "testJsBridgeUrl"
        val embeddedMessagingServiceUrl = "testEmbeddedMessagingServiceUrl"
        val clientId = "testClientId"
        val configResponse = RemoteConfigResponse(
            ServiceUrls(
                clientService = clientServiceUrl,
                eventService = eventServiceUrl,
                deepLinkService = deeplinkServiceUrl,
                embeddedMessagingService = embeddedMessagingServiceUrl,
                jsBridgeUrl = jsBridgeUrl
            ), LogLevel.Debug,
            LuckyLogger(LogLevel.Error, 1.0),
            RemoteConfigFeatures(
                mobileEngage = true,
                embeddedMessaging = true,
                jsBridgeSignatureCheck = true
            ),
            overrides = mapOf(
                "differentClientId" to RemoteConfig(
                    ServiceUrls(clientService = "differentClientServiceUrl")
                )
            )
        )
        everySuspend { mockDeviceInfoCollector.getClientId() } returns clientId
        every { mockRandomProvider.provide() } returns 0.1
        every { mockSdkContext.defaultUrls = capture(defaultUrlSlot) } returns Unit


        remoteConfigResponseHandler.handle(configResponse)

        defaultUrlSlot.get().clientServiceBaseUrl shouldBe clientServiceUrl
        defaultUrlSlot.get().eventServiceBaseUrl shouldBe eventServiceUrl
        defaultUrlSlot.get().deepLinkBaseUrl shouldBe deeplinkServiceUrl
        defaultUrlSlot.get().embeddedMessagingBaseUrl shouldBe embeddedMessagingServiceUrl
        defaultUrlSlot.get().jsBridgeUrl shouldBe jsBridgeUrl
        verify { mockSdkContext.remoteLogLevel = LogLevel.Error }
        mockSdkContext.features.size shouldBe 3
        mockSdkContext.features shouldContainAll listOf(
            MobileEngage,
            EmbeddedMessaging,
            JsBridgeSignatureCheck
        )
    }

    @Test
    fun testHandleAppCodeBasedConfigs_shouldFallbackToJsBridgeSignatureCheck_evenIfJsBridgeIsNotDefined() =
        runTest {
            val defaultUrlSlot = slot<DefaultUrlsApi>()

            val configResponse = RemoteConfigResponse(
                ServiceUrls(

                ),
                LogLevel.Debug,
                LuckyLogger(LogLevel.Error, 1.0),
                RemoteConfigFeatures(

                ),
            )
            every { mockRandomProvider.provide() } returns 0.1
            every { mockSdkContext.defaultUrls = capture(defaultUrlSlot) } returns Unit


            remoteConfigResponseHandler.handle(configResponse)

            verify { mockSdkContext.remoteLogLevel = LogLevel.Error }
            mockSdkContext.features shouldBe listOf(
                JsBridgeSignatureCheck
            )
        }

    @Test
    fun testHandleGlobalConfig() = runTest {
        val defaultUrlSlot = slot<DefaultUrlsApi>()
        val clientServiceUrl = "testClientServiceUrl"
        val clientId = "testClientId"
        val configResponse = RemoteConfigResponse(
            ServiceUrls(
                clientService = clientServiceUrl
            ), LogLevel.Debug,
            LuckyLogger(LogLevel.Error, 1.0),
            RemoteConfigFeatures(mobileEngage = true, jsBridgeSignatureCheck = false),
            overrides = mapOf(
                "differentClientId" to RemoteConfig(
                    ServiceUrls(clientService = "differentClientServiceUrl")
                )
            )
        )
        every { mockSdkContext.defaultUrls = capture(defaultUrlSlot) } returns Unit
        everySuspend { mockDeviceInfoCollector.getClientId() } returns clientId
        every { mockRandomProvider.provide() } returns 0.1

        remoteConfigResponseHandler.handle(configResponse)

        defaultUrlSlot.get().clientServiceBaseUrl shouldBe clientServiceUrl
        verify { mockSdkContext.remoteLogLevel = LogLevel.Error }
        mockSdkContext.features shouldBe listOf(MobileEngage)
    }

}