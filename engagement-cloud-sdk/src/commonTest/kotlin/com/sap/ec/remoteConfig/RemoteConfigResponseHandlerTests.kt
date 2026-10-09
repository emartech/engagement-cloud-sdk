package com.sap.ec.remoteConfig

import com.sap.ec.context.Features.EmbeddedMessaging
import com.sap.ec.context.Features.JsBridgeSignatureCheck
import com.sap.ec.context.Features.MobileEngage
import com.sap.ec.context.SdkContextApi
import com.sap.ec.context.ServiceUrls.FeatureUrls
import com.sap.ec.context.ServiceUrls.GlobalFeatureUrls
import com.sap.ec.context.ServiceUrlsApi
import com.sap.ec.core.device.DeviceInfoCollectorApi
import com.sap.ec.core.exceptions.SdkException
import com.sap.ec.core.log.LogConfigHolderApi
import com.sap.ec.core.log.LogLevel
import com.sap.ec.core.log.SdkLogger
import com.sap.ec.core.providers.DoubleProvider
import com.sap.ec.mobileengage.embeddedmessaging.EmbeddedMessagingContextApi
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture.Companion.slot
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class RemoteConfigResponseHandlerTests {
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var mockLogConfigHolder: LogConfigHolderApi
    private lateinit var serviceUrls: ServiceUrlsApi
    private lateinit var mockDeviceInfoCollector: DeviceInfoCollectorApi
    private lateinit var mockRandomProvider: DoubleProvider
    private lateinit var mockEmbeddedMessagingContext: EmbeddedMessagingContextApi
    private lateinit var remoteConfigResponseHandler: RemoteConfigResponseHandler

    @BeforeTest
    fun setUp() {
        mockSdkContext = mock(MockMode.autofill)
        serviceUrls = mock(MockMode.autofill)
        every { mockSdkContext.serviceUrls } returns serviceUrls

        mockLogConfigHolder = mock(MockMode.autofill)

        everySuspend { mockSdkContext.sdkDispatcher } returns StandardTestDispatcher()
        every { mockSdkContext.features } returns mutableSetOf(JsBridgeSignatureCheck)

        mockDeviceInfoCollector = mock(MockMode.autofill)
        mockRandomProvider = mock(MockMode.autofill)
        mockEmbeddedMessagingContext = mock(MockMode.autofill)
        every { mockRandomProvider.provide() } returns 0.5

        remoteConfigResponseHandler = RemoteConfigResponseHandler(
            mockDeviceInfoCollector,
            mockLogConfigHolder,
            mockSdkContext,
            mockRandomProvider,
            mockEmbeddedMessagingContext,
            SdkLogger(
                "TestLoggerName",
                mock(MockMode.autofill),
                logConfigHolder = mock(MockMode.autofill)
            )
        )
    }

    @Test
    fun testHandleAppCodeBasedConfigs() = runTest {
        val featureUrlsSlot = slot<ServiceUrlsApi.FeatureUrlsApi>()
        val clientServiceUrl = "testClientServiceUrl"
        val eventServiceUrl = "testEventServiceUrl"
        val embeddedMessagingServiceUrl = "testEmbeddedMessagingServiceUrl"
        val loggingServiceUrl = "testLoggingServiceUrl"
        val clientId = "testClientId"
        val configResponse = RemoteConfigResponse(
            serviceUrls = RemoteConfigServiceUrls(
                clientService = clientServiceUrl,
                eventService = eventServiceUrl,
                embeddedMessagingService = embeddedMessagingServiceUrl,
                loggingService = loggingServiceUrl
            ),
            logLevel = LogLevel.Debug,
            luckyLogger = LuckyLogger(LogLevel.Error, 1.0),
            features = RemoteConfigFeatures(
                mobileEngage = true,
                embeddedMessaging = true,
                jsBridgeSignatureCheck = true
            ),
            overrides = mapOf(
                "differentClientId" to RemoteConfigOverride(
                    RemoteConfigOptionalServiceUrls(clientService = "differentClientServiceUrl")
                )
            )
        )
        everySuspend { mockDeviceInfoCollector.getClientId() } returns clientId
        every { mockSdkContext.serviceUrls.featureUrls = capture(featureUrlsSlot) } returns Unit

        remoteConfigResponseHandler.handle(configResponse)

        featureUrlsSlot.get() shouldBe FeatureUrls(
            clientServiceBaseUrl = clientServiceUrl,
            eventServiceBaseUrl = eventServiceUrl,
            embeddedMessagingBaseUrl = embeddedMessagingServiceUrl,
            loggingUrl = loggingServiceUrl
        )
        verify { mockLogConfigHolder.remoteLogLevel = LogLevel.Error }
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
            val serviceUrlSlot = slot<ServiceUrlsApi>()

            val configResponse = RemoteConfigResponse(
                serviceUrls = RemoteConfigServiceUrls(
                    clientService = "",
                    eventService = "",
                    embeddedMessagingService = "",
                    loggingService = ""
                ),
                logLevel = LogLevel.Debug,
                luckyLogger = LuckyLogger(LogLevel.Error, 1.0),
                features = RemoteConfigFeatures(

                ),
            )
            every { mockSdkContext.serviceUrls = capture(serviceUrlSlot) } returns Unit

            remoteConfigResponseHandler.handle(configResponse)

            verify { mockLogConfigHolder.remoteLogLevel = LogLevel.Error }
            mockSdkContext.features shouldBe listOf(
                JsBridgeSignatureCheck
            )
        }

    @Test
    fun testHandleGlobalConfig() = runTest {
        val globalFeatureUrlsSlot = slot<ServiceUrlsApi.GlobalFeatureUrlsApi>()
        val deeplinkServiceUrl = "testDeepLinkServiceUrl"
        val jsBridgeUrl = "testJsBridgeUrl"
        val jsBridgeSignatureUrl = "testJsBridgeSignatureUrl"
        val configResponse = RemoteConfigResponse(
            globalServiceUrls = RemoteConfigGlobalServiceUrls(
                deepLinkService = deeplinkServiceUrl,
                jsBridgeUrl = jsBridgeUrl,
                jsBridgeSignatureUrl = jsBridgeSignatureUrl
            ),
            logLevel = LogLevel.Debug,
            luckyLogger = LuckyLogger(LogLevel.Error, 1.0),
            features = RemoteConfigFeatures(mobileEngage = true, jsBridgeSignatureCheck = false),
        )
        every { mockSdkContext.serviceUrls.globalFeatureUrls = capture(globalFeatureUrlsSlot) } returns Unit

        remoteConfigResponseHandler.handle(configResponse)

        globalFeatureUrlsSlot.get() shouldBe GlobalFeatureUrls(
            deepLinkBaseUrl = deeplinkServiceUrl,
            jsBridgeUrl = jsBridgeUrl,
            jsBridgeSignatureUrl = jsBridgeSignatureUrl
        )
        verify { mockLogConfigHolder.remoteLogLevel = LogLevel.Error }
        mockSdkContext.features shouldBe listOf(MobileEngage)
    }

    @Test
    fun testHandleGlobalServiceUrls_shouldNotApplyNull_whenSdkDefaultFalse() = runTest {
        val globalFeatureUrls: ServiceUrlsApi.GlobalFeatureUrlsApi = mock(MockMode.autofill)
        every { serviceUrls.globalFeatureUrls } returns globalFeatureUrls

        val configResponse = RemoteConfigResponse()

        remoteConfigResponseHandler.handle(configResponse, false)

        verify(VerifyMode.exactly(0)) { mockSdkContext.serviceUrls.globalFeatureUrls = null }
    }

    @Test
    fun testHandleGlobalServiceUrls_shouldApplyNull_whenSdkDefaultTrue() = runTest {
        val globalFeatureUrls: ServiceUrlsApi.GlobalFeatureUrlsApi = mock(MockMode.autofill)
        every { serviceUrls.globalFeatureUrls } returns globalFeatureUrls

        val configResponse = RemoteConfigResponse()

        remoteConfigResponseHandler.handle(configResponse, true)

        verify(VerifyMode.exactly(1)) { mockSdkContext.serviceUrls.globalFeatureUrls = null }
    }

    @Test
    fun testHandleServiceUrls_shouldNotApplyNull_whenSdkDefaultFalse() = runTest {
        val featureUrls: ServiceUrlsApi.FeatureUrlsApi = mock(MockMode.autofill)
        every { serviceUrls.featureUrls } returns featureUrls

        val configResponse = RemoteConfigResponse()

        remoteConfigResponseHandler.handle(configResponse, false)

        verify(VerifyMode.exactly(0)) { mockSdkContext.serviceUrls.featureUrls = null }
    }

    @Test
    fun testHandleServiceUrls_shouldApplyNull_whenSdkDefaultTrue() = runTest {
        val featureUrls: ServiceUrlsApi.FeatureUrlsApi = mock(MockMode.autofill)
        every { serviceUrls.featureUrls } returns featureUrls

        val configResponse = RemoteConfigResponse()

        remoteConfigResponseHandler.handle(configResponse, true)

        verify(VerifyMode.exactly(1)) { mockSdkContext.serviceUrls.featureUrls = null }
    }

    @Test
    fun testHandleServiceUrls_shouldApplyOverride() = runTest {
        val featureUrlsSlot = slot<ServiceUrlsApi.FeatureUrlsApi>()
        val clientServiceUrl = "testClientServiceUrl"
        val eventServiceUrl = "testEventServiceUrl"
        val embeddedMessagingServiceUrl = "testEmbeddedMessagingServiceUrl"
        val loggingServiceUrl = "testLoggingServiceUrl"
        val clientId = "testClientId"
        val configResponse = RemoteConfigResponse(
            serviceUrls = RemoteConfigServiceUrls(
                clientService = "url",
                eventService = "url",
                embeddedMessagingService = "url",
                loggingService = "url"
            ),
            overrides = mapOf(
                clientId to RemoteConfigOverride(
                    RemoteConfigOptionalServiceUrls(
                        clientService = clientServiceUrl,
                        eventService = eventServiceUrl,
                        embeddedMessagingService = embeddedMessagingServiceUrl,
                        loggingService = loggingServiceUrl
                    )
                )
            )
        )
        every { mockSdkContext.serviceUrls.featureUrls = capture(featureUrlsSlot) } returns Unit
        everySuspend { mockDeviceInfoCollector.getClientId() } returns clientId

        remoteConfigResponseHandler.handle(configResponse)

        featureUrlsSlot.get() shouldBe FeatureUrls(
            clientServiceBaseUrl = clientServiceUrl,
            eventServiceBaseUrl = eventServiceUrl,
            embeddedMessagingBaseUrl = embeddedMessagingServiceUrl,
            loggingUrl = loggingServiceUrl
        )
    }

    @Test
    fun testHandleLogLevel() = runTest {
        val configResponse = RemoteConfigResponse(
            logLevel = LogLevel.Info
        )

        remoteConfigResponseHandler.handle(configResponse)

        verify { mockLogConfigHolder.remoteLogLevel = LogLevel.Info }
    }

    @Test
    fun testHandleLogLevel_shouldApplyLuckyLogger() = runTest {
        val configResponse = RemoteConfigResponse(
            logLevel = LogLevel.Debug,
            luckyLogger = LuckyLogger(LogLevel.Error, 1.0),
        )

        remoteConfigResponseHandler.handle(configResponse)

        verify { mockLogConfigHolder.remoteLogLevel = LogLevel.Error }
    }

    @Test
    fun testHandleLogLevel_shouldApplyOverride() = runTest {
        val clientId = "testClientId"
        val configResponse = RemoteConfigResponse(
            logLevel = LogLevel.Debug,
            luckyLogger = LuckyLogger(LogLevel.Error, 1.0),
            overrides = mapOf(
                clientId to RemoteConfigOverride(
                    logLevel = LogLevel.Info,
                )
            )
        )
        everySuspend { mockDeviceInfoCollector.getClientId() } returns clientId

        remoteConfigResponseHandler.handle(configResponse)

        verify { mockLogConfigHolder.remoteLogLevel = LogLevel.Info }
    }

    @Test
    fun testHandleEmbeddedMessagingConfig_shouldApplyBothProperties() = runTest {
        val batchSize = 20
        val frequencyCap = 10
        val configResponse = RemoteConfigResponse(
            embeddedMessagingConfig = EmbeddedMessagingConfig(
                tagUpdateBatchSize = batchSize,
                tagUpdateFrequencyCapSeconds = frequencyCap
            )
        )

        remoteConfigResponseHandler.handle(configResponse)

        verify { mockEmbeddedMessagingContext.tagUpdateBatchSize = batchSize }
        verify { mockEmbeddedMessagingContext.tagUpdateFrequencyCapSeconds = frequencyCap }
    }

    @Test
    fun testHandleEmbeddedMessagingConfig_shouldApplyOnlyBatchSize() = runTest {
        val batchSize = 15
        val configResponse = RemoteConfigResponse(
            embeddedMessagingConfig = EmbeddedMessagingConfig(
                tagUpdateBatchSize = batchSize,
                tagUpdateFrequencyCapSeconds = null
            )
        )

        remoteConfigResponseHandler.handle(configResponse)

        verify { mockEmbeddedMessagingContext.tagUpdateBatchSize = batchSize }
    }

    @Test
    fun testHandleEmbeddedMessagingConfig_shouldApplyOnlyFrequencyCap() = runTest {
        val frequencyCap = 30
        val configResponse = RemoteConfigResponse(
            embeddedMessagingConfig = EmbeddedMessagingConfig(
                tagUpdateBatchSize = null,
                tagUpdateFrequencyCapSeconds = frequencyCap
            )
        )

        remoteConfigResponseHandler.handle(configResponse)

        verify { mockEmbeddedMessagingContext.tagUpdateFrequencyCapSeconds = frequencyCap }
    }

    @Test
    fun testHandleEmbeddedMessagingConfig_shouldNotApplyWhenConfigIsNull() = runTest {
        val configResponse = RemoteConfigResponse(
            embeddedMessagingConfig = null
        )

        remoteConfigResponseHandler.handle(configResponse)

        verify(VerifyMode.exactly(0)) {
            mockEmbeddedMessagingContext.tagUpdateBatchSize = any()
            mockEmbeddedMessagingContext.tagUpdateFrequencyCapSeconds = any()
        }
    }

    @Test
    fun testHandleEmbeddedMessagingConfig_shouldApplyOverride() = runTest {
        val globalBatchSize = 10
        val globalFrequencyCap = 5
        val overrideBatchSize = 25
        val overrideFrequencyCap = 15
        val clientId = "testClientId"
        val configResponse = RemoteConfigResponse(
            embeddedMessagingConfig = EmbeddedMessagingConfig(
                tagUpdateBatchSize = globalBatchSize,
                tagUpdateFrequencyCapSeconds = globalFrequencyCap
            ),
            overrides = mapOf(
                clientId to RemoteConfigOverride(
                    embeddedMessagingConfig = EmbeddedMessagingConfig(
                        tagUpdateBatchSize = overrideBatchSize,
                        tagUpdateFrequencyCapSeconds = overrideFrequencyCap
                    )
                )
            )
        )
        everySuspend { mockDeviceInfoCollector.getClientId() } returns clientId

        remoteConfigResponseHandler.handle(configResponse)

        verify { mockEmbeddedMessagingContext.tagUpdateBatchSize = overrideBatchSize }
        verify { mockEmbeddedMessagingContext.tagUpdateFrequencyCapSeconds = overrideFrequencyCap }
    }

    @Test
    fun testHandleEmbeddedMessagingConfig_shouldApplyOnlyOverrideBatchSize() = runTest {
        val globalBatchSize = 10
        val globalFrequencyCap = 5
        val overrideBatchSize = 25
        val clientId = "testClientId"
        val configResponse = RemoteConfigResponse(
            embeddedMessagingConfig = EmbeddedMessagingConfig(
                tagUpdateBatchSize = globalBatchSize,
                tagUpdateFrequencyCapSeconds = globalFrequencyCap
            ),
            overrides = mapOf(
                clientId to RemoteConfigOverride(
                    embeddedMessagingConfig = EmbeddedMessagingConfig(
                        tagUpdateBatchSize = overrideBatchSize,
                        tagUpdateFrequencyCapSeconds = null
                    )
                )
            )
        )
        everySuspend { mockDeviceInfoCollector.getClientId() } returns clientId

        remoteConfigResponseHandler.handle(configResponse)

        verify { mockEmbeddedMessagingContext.tagUpdateBatchSize = overrideBatchSize }
        verify { mockEmbeddedMessagingContext.tagUpdateFrequencyCapSeconds = globalFrequencyCap }
    }

    @Test
    fun testHandle_shouldAssignSdkManagementApplicationCodeValidationRegex_toSdkContext() =
        runTest {
            val expectedRegex = "^CUSTOM-[A-Z0-9]+$"
            val regexSlot = slot<Regex?>()
            val configResponse = RemoteConfigResponse(
                globalRemoteConfigApplicationCodeValidationRegex = expectedRegex
            )
            every {
                mockSdkContext.globalRemoteConfigApplicationCodeValidationRegex = capture(regexSlot)
            } returns Unit

            remoteConfigResponseHandler.handle(configResponse)

            regexSlot.get()!!.pattern shouldBe expectedRegex
        }

    @Test
    fun testHandle_shouldDiscardInvalidRegex_andAssignNull() = runTest {
        val invalidRegex = "[invalid("
        val configResponse = RemoteConfigResponse(
            globalRemoteConfigApplicationCodeValidationRegex = invalidRegex
        )

        remoteConfigResponseHandler.handle(configResponse)

        verify { mockSdkContext.globalRemoteConfigApplicationCodeValidationRegex = null }
    }

    @Test
    fun testHandle_shouldNotResetRegex_whenFieldIsAbsentFromResponse() = runTest {
        val configResponse = RemoteConfigResponse()

        remoteConfigResponseHandler.handle(configResponse)

        verify(VerifyMode.exactly(0)) {
            mockSdkContext.globalRemoteConfigApplicationCodeValidationRegex = any()
        }
    }

    @Test
    fun testApplyFeatures_shouldApplyOverride() = runTest {
        val clientId = "testClientId"
        val configResponse = RemoteConfigResponse(
            features = RemoteConfigFeatures(
                jsBridgeSignatureCheck = true,
                mobileEngage = false,
                embeddedMessaging = true
            ),
            overrides = mapOf(
                clientId to RemoteConfigOverride(
                    features = RemoteConfigFeatures(
                        jsBridgeSignatureCheck = false,
                        mobileEngage = true,
                        embeddedMessaging = false
                    ),
                )
            )
        )
        everySuspend { mockDeviceInfoCollector.getClientId() } returns clientId

        remoteConfigResponseHandler.handle(configResponse)

        mockSdkContext.features shouldBe listOf(MobileEngage)
    }

    @Test
    fun testApplyFeatures_shouldFail_whenSdkDisabledIsTrue() = runTest {
        val configResponse = RemoteConfigResponse(
            features = RemoteConfigFeatures(jsBridgeSignatureCheck = true, mobileEngage = true, embeddedMessaging = true),
            disabled = true
        )

        assertFailsWith<SdkException.SdkDisabledException> {
            remoteConfigResponseHandler.handle(configResponse)
        }
    }

    @Test
    fun testApplyFeatures_shouldApplyingFeatures_whenSdkDisabledIsFalse() = runTest {
        val configResponse = RemoteConfigResponse(
            features = RemoteConfigFeatures(jsBridgeSignatureCheck = true, mobileEngage = true, embeddedMessaging = true),
            disabled = false
        )

        remoteConfigResponseHandler.handle(configResponse)

        mockSdkContext.features shouldBe listOf(JsBridgeSignatureCheck, MobileEngage, EmbeddedMessaging)
    }

    @Test
    fun testApplyFeatures_shouldApplyingFeatures_whenSdkDisabledIsNull() = runTest {
        val configResponse = RemoteConfigResponse(
            features = RemoteConfigFeatures(jsBridgeSignatureCheck = true, mobileEngage = true, embeddedMessaging = true),
            disabled = null
        )

        remoteConfigResponseHandler.handle(configResponse)

        mockSdkContext.features shouldBe listOf(JsBridgeSignatureCheck, MobileEngage, EmbeddedMessaging)
    }
}