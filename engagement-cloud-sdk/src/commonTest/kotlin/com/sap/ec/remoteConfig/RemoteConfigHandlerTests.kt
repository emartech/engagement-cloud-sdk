package com.sap.ec.remoteConfig

import com.sap.ec.context.Features.EmbeddedMessaging
import com.sap.ec.context.Features.JsBridgeSignatureCheck
import com.sap.ec.context.Features.MobileEngage
import com.sap.ec.context.SdkContextApi
import com.sap.ec.context.ServiceUrls
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
class RemoteConfigHandlerTests {
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var mockLogConfigHolder: LogConfigHolderApi
    private lateinit var mockDeviceInfoCollector: DeviceInfoCollectorApi
    private lateinit var mockRandomProvider: DoubleProvider
    private lateinit var mockEmbeddedMessagingContext: EmbeddedMessagingContextApi
    private lateinit var remoteConfigHandler: RemoteConfigHandler

    @BeforeTest
    fun setUp() {
        mockSdkContext = mock(MockMode.autofill)
        every { mockSdkContext.serviceUrls } returns ServiceUrls(remoteConfigBaseUrl = "")

        mockLogConfigHolder = mock(MockMode.autofill)

        everySuspend { mockSdkContext.sdkDispatcher } returns StandardTestDispatcher()
        every { mockSdkContext.features } returns mutableSetOf(JsBridgeSignatureCheck)

        mockDeviceInfoCollector = mock(MockMode.autofill)
        mockRandomProvider = mock(MockMode.autofill)
        mockEmbeddedMessagingContext = mock(MockMode.autofill)
        every { mockRandomProvider.provide() } returns 0.5

        remoteConfigHandler = RemoteConfigHandler(
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
    fun testHandleRemoteConfigs() = runTest {
        every { mockSdkContext.serviceUrls } returns ServiceUrls(
            remoteConfigBaseUrl = "",
            globalServiceUrls = ServiceUrls.Global(
                deepLinkBaseUrl = "",
                jsBridgeUrl = "",
                jsBridgeSignatureUrl = ""
            ),
            appCodeBasedServiceUrls = ServiceUrls.AppCodeBased(
                clientServiceBaseUrl = "",
                eventServiceBaseUrl = "",
                embeddedMessagingBaseUrl = "",
                loggingUrl = ""
            )
        )

        val config = RemoteConfig.Default(
            logLevel = LogLevel.Info,
            features = RemoteConfigFeatures(
                mobileEngage = true,
                embeddedMessaging = true,
                jsBridgeSignatureCheck = false
            ),
            embeddedMessagingConfig = EmbeddedMessagingConfig(
                tagUpdateBatchSize = 111,
                tagUpdateFrequencyCapSeconds = 222
            )
        )

        remoteConfigHandler.handle(config)

        mockSdkContext.serviceUrls.globalServiceUrls shouldBe null
        mockSdkContext.serviceUrls.appCodeBasedServiceUrls shouldBe null
        verify { mockLogConfigHolder.remoteLogLevel = LogLevel.Info }
        mockSdkContext.features shouldBe listOf(MobileEngage, EmbeddedMessaging)
        verify { mockEmbeddedMessagingContext.tagUpdateBatchSize = 111 }
        verify { mockEmbeddedMessagingContext.tagUpdateFrequencyCapSeconds = 222 }
    }

    @Test
    fun testHandleAppCodeBasedConfigs() = runTest {
        val clientServiceUrl = "testClientServiceUrl"
        val eventServiceUrl = "testEventServiceUrl"
        val embeddedMessagingServiceUrl = "testEmbeddedMessagingServiceUrl"
        val loggingServiceUrl = "testLoggingServiceUrl"
        val clientId = "testClientId"
        val config = RemoteConfig.Response.AppCodeBased(
            appCodeBasedServiceUrls = RemoteConfigServiceUrls.AppCodeBased(
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
                "differentClientId" to RemoteConfig.Response.Override(
                    RemoteConfigServiceUrls.Optional(clientService = "differentClientServiceUrl")
                )
            )
        )
        everySuspend { mockDeviceInfoCollector.getClientId() } returns clientId

        remoteConfigHandler.handle(config)

        mockSdkContext.serviceUrls.appCodeBasedServiceUrls shouldBe ServiceUrls.AppCodeBased(
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
            val config = RemoteConfig.Response.AppCodeBased(
                appCodeBasedServiceUrls = RemoteConfigServiceUrls.AppCodeBased(
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

            remoteConfigHandler.handle(config)

            verify { mockLogConfigHolder.remoteLogLevel = LogLevel.Error }
            mockSdkContext.features shouldBe listOf(
                JsBridgeSignatureCheck
            )
        }

    @Test
    fun testHandleGlobalConfig() = runTest {
        val deeplinkServiceUrl = "testDeepLinkServiceUrl"
        val jsBridgeUrl = "testJsBridgeUrl"
        val jsBridgeSignatureUrl = "testJsBridgeSignatureUrl"
        val config = RemoteConfig.Response.Global(
            globalServiceUrls = RemoteConfigServiceUrls.Global(
                deepLinkService = deeplinkServiceUrl,
                jsBridgeUrl = jsBridgeUrl,
                jsBridgeSignatureUrl = jsBridgeSignatureUrl
            ),
            logLevel = LogLevel.Debug,
            luckyLogger = LuckyLogger(LogLevel.Error, 1.0),
            features = RemoteConfigFeatures(mobileEngage = true, jsBridgeSignatureCheck = false),
        )

        remoteConfigHandler.handle(config)

        mockSdkContext.serviceUrls.globalServiceUrls shouldBe ServiceUrls.Global(
            deepLinkBaseUrl = deeplinkServiceUrl,
            jsBridgeUrl = jsBridgeUrl,
            jsBridgeSignatureUrl = jsBridgeSignatureUrl
        )
        verify { mockLogConfigHolder.remoteLogLevel = LogLevel.Error }
        mockSdkContext.features shouldBe listOf(MobileEngage)
    }

    @Test
    fun testHandleAppCodeBasedServiceUrls_shouldApplyOverride() = runTest {
        val clientServiceUrl = "testClientServiceUrl"
        val eventServiceUrl = "testEventServiceUrl"
        val embeddedMessagingServiceUrl = "testEmbeddedMessagingServiceUrl"
        val loggingServiceUrl = "testLoggingServiceUrl"
        val clientId = "testClientId"
        val config = RemoteConfig.Response.AppCodeBased(
            appCodeBasedServiceUrls = RemoteConfigServiceUrls.AppCodeBased(
                clientService = "testUrl",
                eventService = "testUrl",
                embeddedMessagingService = "testUrl",
                loggingService = "testUrl"
            ),
            overrides = mapOf(
                clientId to RemoteConfig.Response.Override(
                    RemoteConfigServiceUrls.Optional(
                        clientService = clientServiceUrl,
                        eventService = eventServiceUrl,
                        embeddedMessagingService = embeddedMessagingServiceUrl,
                        loggingService = loggingServiceUrl
                    )
                )
            )
        )
        everySuspend { mockDeviceInfoCollector.getClientId() } returns clientId

        remoteConfigHandler.handle(config)

        mockSdkContext.serviceUrls.appCodeBasedServiceUrls shouldBe ServiceUrls.AppCodeBased(
            clientServiceBaseUrl = clientServiceUrl,
            eventServiceBaseUrl = eventServiceUrl,
            embeddedMessagingBaseUrl = embeddedMessagingServiceUrl,
            loggingUrl = loggingServiceUrl
        )
    }

    @Test
    fun testHandleLogLevel() = runTest {
        val config = RemoteConfig.Default(
            logLevel = LogLevel.Info,
            features = RemoteConfigFeatures(),
            embeddedMessagingConfig = EmbeddedMessagingConfig()
        )

        remoteConfigHandler.handle(config)

        verify { mockLogConfigHolder.remoteLogLevel = LogLevel.Info }
    }

    @Test
    fun testHandleLogLevel_shouldApplyLuckyLogger() = runTest {
        val config = RemoteConfig.Default(
            logLevel = LogLevel.Debug,
            luckyLogger = LuckyLogger(LogLevel.Error, 1.0),
            features = RemoteConfigFeatures(),
            embeddedMessagingConfig = EmbeddedMessagingConfig()
        )

        remoteConfigHandler.handle(config)

        verify { mockLogConfigHolder.remoteLogLevel = LogLevel.Error }
    }

    @Test
    fun testHandleLogLevel_shouldApplyOverride() = runTest {
        val clientId = "testClientId"
        val config = RemoteConfig.Response.AppCodeBased(
            appCodeBasedServiceUrls = RemoteConfigServiceUrls.AppCodeBased(
                clientService = "",
                eventService = "",
                embeddedMessagingService = "",
                loggingService = ""
            ),
            logLevel = LogLevel.Debug,
            luckyLogger = LuckyLogger(LogLevel.Error, 1.0),
            overrides = mapOf(
                clientId to RemoteConfig.Response.Override(
                    logLevel = LogLevel.Info,
                )
            )
        )
        everySuspend { mockDeviceInfoCollector.getClientId() } returns clientId

        remoteConfigHandler.handle(config)

        verify { mockLogConfigHolder.remoteLogLevel = LogLevel.Info }
    }

    @Test
    fun testHandleEmbeddedMessagingConfig_shouldApplyBothProperties() = runTest {
        val batchSize = 20
        val frequencyCap = 10
        val config = RemoteConfig.Default(
            logLevel = LogLevel.Error,
            features = RemoteConfigFeatures(),
            embeddedMessagingConfig = EmbeddedMessagingConfig(
                tagUpdateBatchSize = batchSize,
                tagUpdateFrequencyCapSeconds = frequencyCap
            )
        )

        remoteConfigHandler.handle(config)

        verify { mockEmbeddedMessagingContext.tagUpdateBatchSize = batchSize }
        verify { mockEmbeddedMessagingContext.tagUpdateFrequencyCapSeconds = frequencyCap }
    }

    @Test
    fun testHandleEmbeddedMessagingConfig_shouldApplyOnlyBatchSize() = runTest {
        val batchSize = 15
        val config = RemoteConfig.Default(
            logLevel = LogLevel.Error,
            features = RemoteConfigFeatures(),
            embeddedMessagingConfig = EmbeddedMessagingConfig(
                tagUpdateBatchSize = batchSize,
                tagUpdateFrequencyCapSeconds = null
            )
        )

        remoteConfigHandler.handle(config)

        verify { mockEmbeddedMessagingContext.tagUpdateBatchSize = batchSize }
    }

    @Test
    fun testHandleEmbeddedMessagingConfig_shouldApplyOnlyFrequencyCap() = runTest {
        val frequencyCap = 30
        val config = RemoteConfig.Default(
            logLevel = LogLevel.Error,
            features = RemoteConfigFeatures(),
            embeddedMessagingConfig = EmbeddedMessagingConfig(
                tagUpdateBatchSize = null,
                tagUpdateFrequencyCapSeconds = frequencyCap
            )
        )

        remoteConfigHandler.handle(config)

        verify { mockEmbeddedMessagingContext.tagUpdateFrequencyCapSeconds = frequencyCap }
    }

    @Test
    fun testHandleEmbeddedMessagingConfig_shouldNotApplyWhenConfigIsNull() = runTest {
        val config = RemoteConfig.Response.Global(
            globalServiceUrls = RemoteConfigServiceUrls.Global(
                deepLinkService = "",
                jsBridgeUrl = "",
                jsBridgeSignatureUrl = ""
            ),
            embeddedMessagingConfig = null
        )

        remoteConfigHandler.handle(config)

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
        val config = RemoteConfig.Response.AppCodeBased(
            appCodeBasedServiceUrls = RemoteConfigServiceUrls.AppCodeBased(
                clientService = "",
                eventService = "",
                embeddedMessagingService = "",
                loggingService = ""
            ),
            embeddedMessagingConfig = EmbeddedMessagingConfig(
                tagUpdateBatchSize = globalBatchSize,
                tagUpdateFrequencyCapSeconds = globalFrequencyCap
            ),
            overrides = mapOf(
                clientId to RemoteConfig.Response.Override(
                    embeddedMessagingConfig = EmbeddedMessagingConfig(
                        tagUpdateBatchSize = overrideBatchSize,
                        tagUpdateFrequencyCapSeconds = overrideFrequencyCap
                    )
                )
            )
        )
        everySuspend { mockDeviceInfoCollector.getClientId() } returns clientId

        remoteConfigHandler.handle(config)

        verify { mockEmbeddedMessagingContext.tagUpdateBatchSize = overrideBatchSize }
        verify { mockEmbeddedMessagingContext.tagUpdateFrequencyCapSeconds = overrideFrequencyCap }
    }

    @Test
    fun testHandleEmbeddedMessagingConfig_shouldApplyOnlyOverrideBatchSize() = runTest {
        val globalBatchSize = 10
        val globalFrequencyCap = 5
        val overrideBatchSize = 25
        val clientId = "testClientId"
        val config = RemoteConfig.Response.AppCodeBased(
            appCodeBasedServiceUrls = RemoteConfigServiceUrls.AppCodeBased(
                clientService = "",
                eventService = "",
                embeddedMessagingService = "",
                loggingService = ""
            ),
            embeddedMessagingConfig = EmbeddedMessagingConfig(
                tagUpdateBatchSize = globalBatchSize,
                tagUpdateFrequencyCapSeconds = globalFrequencyCap
            ),
            overrides = mapOf(
                clientId to RemoteConfig.Response.Override(
                    embeddedMessagingConfig = EmbeddedMessagingConfig(
                        tagUpdateBatchSize = overrideBatchSize,
                        tagUpdateFrequencyCapSeconds = null
                    )
                )
            )
        )
        everySuspend { mockDeviceInfoCollector.getClientId() } returns clientId

        remoteConfigHandler.handle(config)

        verify { mockEmbeddedMessagingContext.tagUpdateBatchSize = overrideBatchSize }
        verify { mockEmbeddedMessagingContext.tagUpdateFrequencyCapSeconds = globalFrequencyCap }
    }

    @Test
    fun testHandle_shouldAssignSdkManagementApplicationCodeValidationRegex_toSdkContext() =
        runTest {
            val expectedRegex = "^CUSTOM-[A-Z0-9]+$"
            val regexSlot = slot<Regex?>()
            val config = RemoteConfig.Response.Global(
                globalRemoteConfigApplicationCodeValidationRegex = expectedRegex,
                globalServiceUrls = RemoteConfigServiceUrls.Global(
                    deepLinkService = "",
                    jsBridgeUrl = "",
                    jsBridgeSignatureUrl = ""
                )
            )
            every {
                mockSdkContext.globalRemoteConfigApplicationCodeValidationRegex = capture(regexSlot)
            } returns Unit

            remoteConfigHandler.handle(config)

            regexSlot.get()!!.pattern shouldBe expectedRegex
        }

    @Test
    fun testHandle_shouldDiscardInvalidRegex_andAssignNull() = runTest {
        val invalidRegex = "[invalid("
        val config = RemoteConfig.Response.Global(
            globalRemoteConfigApplicationCodeValidationRegex = invalidRegex,
            globalServiceUrls = RemoteConfigServiceUrls.Global(
                deepLinkService = "",
                jsBridgeUrl = "",
                jsBridgeSignatureUrl = ""
            )
        )

        remoteConfigHandler.handle(config)

        verify { mockSdkContext.globalRemoteConfigApplicationCodeValidationRegex = null }
    }

    @Test
    fun testHandle_shouldNotResetRegex_whenFieldIsAbsentFromResponse() = runTest {
        val config = RemoteConfig.Response.Global(
            globalServiceUrls = RemoteConfigServiceUrls.Global(
                deepLinkService = "",
                jsBridgeUrl = "",
                jsBridgeSignatureUrl = ""
            )
        )

        remoteConfigHandler.handle(config)

        verify(VerifyMode.exactly(0)) {
            mockSdkContext.globalRemoteConfigApplicationCodeValidationRegex = any()
        }
    }

    @Test
    fun testApplyFeatures_shouldApplyOverride() = runTest {
        val clientId = "testClientId"
        val config = RemoteConfig.Response.AppCodeBased(
            appCodeBasedServiceUrls = RemoteConfigServiceUrls.AppCodeBased(
                clientService = "",
                eventService = "",
                embeddedMessagingService = "",
                loggingService = ""
            ),
            features = RemoteConfigFeatures(
                jsBridgeSignatureCheck = true,
                mobileEngage = false,
                embeddedMessaging = true
            ),
            overrides = mapOf(
                clientId to RemoteConfig.Response.Override(
                    features = RemoteConfigFeatures(
                        jsBridgeSignatureCheck = false,
                        mobileEngage = true,
                        embeddedMessaging = false
                    ),
                )
            )
        )
        everySuspend { mockDeviceInfoCollector.getClientId() } returns clientId

        remoteConfigHandler.handle(config)

        mockSdkContext.features shouldBe listOf(MobileEngage)
    }

    @Test
    fun testApplyFeatures_shouldFail_whenSdkDisabledIsTrue() = runTest {
        val config = RemoteConfig.Response.AppCodeBased(
            disabled = true,
            appCodeBasedServiceUrls = RemoteConfigServiceUrls.AppCodeBased(
                clientService = "",
                eventService = "",
                embeddedMessagingService = "",
                loggingService = ""
            ),
            features = RemoteConfigFeatures(jsBridgeSignatureCheck = true, mobileEngage = true, embeddedMessaging = true)
        )

        assertFailsWith<SdkException.SdkDisabledException> {
            remoteConfigHandler.handle(config)
        }
    }

    @Test
    fun testApplyFeatures_shouldApplyingFeatures_whenSdkDisabledIsFalse() = runTest {
        val config = RemoteConfig.Response.AppCodeBased(
            disabled = false,
            appCodeBasedServiceUrls = RemoteConfigServiceUrls.AppCodeBased(
                clientService = "",
                eventService = "",
                embeddedMessagingService = "",
                loggingService = ""
            ),
            features = RemoteConfigFeatures(jsBridgeSignatureCheck = true, mobileEngage = true, embeddedMessaging = true)
        )

        remoteConfigHandler.handle(config)

        mockSdkContext.features shouldBe listOf(JsBridgeSignatureCheck, MobileEngage, EmbeddedMessaging)
    }

    @Test
    fun testApplyFeatures_shouldApplyingFeatures_whenSdkDisabledIsNull() = runTest {
        val config = RemoteConfig.Response.AppCodeBased(
            disabled = null,
            appCodeBasedServiceUrls = RemoteConfigServiceUrls.AppCodeBased(
                clientService = "",
                eventService = "",
                embeddedMessagingService = "",
                loggingService = ""
            ),
            features = RemoteConfigFeatures(jsBridgeSignatureCheck = true, mobileEngage = true, embeddedMessaging = true)
        )

        remoteConfigHandler.handle(config)

        mockSdkContext.features shouldBe listOf(JsBridgeSignatureCheck, MobileEngage, EmbeddedMessaging)
    }
}