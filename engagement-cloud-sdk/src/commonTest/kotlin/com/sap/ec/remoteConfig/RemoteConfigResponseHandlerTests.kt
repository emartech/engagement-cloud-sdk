package com.sap.ec.remoteConfig

import com.sap.ec.context.DefaultUrls
import com.sap.ec.context.DefaultUrlsApi
import com.sap.ec.context.Features
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
        defaultUrls = DefaultUrls("", "", "", "", "", "", "")
        every { mockSdkContext.defaultUrls } returns defaultUrls

        everySuspend { mockSdkContext.sdkDispatcher } returns StandardTestDispatcher()
        every { mockSdkContext.features } returns mutableSetOf()

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
        val clientId = "testClientId"
        val configResponse = RemoteConfigResponse(
            ServiceUrls(
                clientService = clientServiceUrl
            ), LogLevel.Debug,
            LuckyLogger(LogLevel.Error, 1.0),
            RemoteConfigFeatures(mobileEngage = true, embeddedMessaging = true),
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
        verify { mockSdkContext.remoteLogLevel = LogLevel.Error }
        mockSdkContext.features shouldBe listOf(Features.MOBILE_ENGAGE, Features.EMBEDDED_MESSAGING)
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
            RemoteConfigFeatures(mobileEngage = true),
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
        mockSdkContext.features shouldBe listOf(Features.MOBILE_ENGAGE)
    }

}