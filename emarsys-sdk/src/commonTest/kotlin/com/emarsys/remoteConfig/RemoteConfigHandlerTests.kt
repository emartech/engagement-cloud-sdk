package com.emarsys.remoteConfig

import com.emarsys.context.DefaultUrls
import com.emarsys.context.Features
import com.emarsys.context.SdkContext
import com.emarsys.core.device.DeviceInfoCollectorApi
import com.emarsys.core.log.ConsoleLogger
import com.emarsys.core.log.LogLevel
import com.emarsys.core.log.SdkLogger
import com.emarsys.core.providers.DoubleProvider
import com.emarsys.networking.clients.remoteConfig.RemoteConfigClientApi
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class RemoteConfigHandlerTests {

    private val mainDispatcher = StandardTestDispatcher()

    init {
        Dispatchers.setMain(mainDispatcher)
    }

    private val sdkContext = SdkContext(
        StandardTestDispatcher(),
        mainDispatcher,
        DefaultUrls("", "", "", "", "", "", ""),
        LogLevel.Debug,
        mutableSetOf(),
        logBreadcrumbsQueueSize = 10
    )
    private lateinit var mockRemoteConfigClient: RemoteConfigClientApi
    private lateinit var mockDeviceInfoCollector: DeviceInfoCollectorApi
    private lateinit var mockRandomProvider: DoubleProvider
    private lateinit var remoteConfigHandler: RemoteConfigHandler

    @BeforeTest
    fun setUp() {
        mockRemoteConfigClient = mock()
        mockDeviceInfoCollector = mock()
        mockRandomProvider = mock()

        remoteConfigHandler = RemoteConfigHandler(
            mockRemoteConfigClient,
            mockDeviceInfoCollector,
            sdkContext,
            mockRandomProvider,
            SdkLogger("TestLoggerName", ConsoleLogger(), sdkContext = mock())
        )
    }

    @Test
    fun testHandleAppCodeBasedConfigs() = runTest {
        val clientServiceUrl = "testClientServiceUrl"
        val predictServiceUrl = "testPredictServiceUrl"
        val clientId = "testClientId"
        val config = RemoteConfigResponse(
            ServiceUrls(
                clientService = clientServiceUrl
            ), LogLevel.Debug,
            LuckyLogger(LogLevel.Error, 1.0),
            RemoteConfigFeatures(mobileEngage = true),
            overrides = mapOf(
                clientId to RemoteConfig(
                    ServiceUrls(predictService = predictServiceUrl)
                ),
                "differentClientId" to RemoteConfig(
                    ServiceUrls(clientService = "differentClientServiceUrl")
                )
            )
        )

        everySuspend { mockDeviceInfoCollector.getClientId() } returns clientId
        everySuspend { mockRemoteConfigClient.fetchRemoteConfig() } returns config
        every { mockRandomProvider.provide() } returns 0.1

        remoteConfigHandler.handleAppCodeBased()

        sdkContext.defaultUrls.clientServiceBaseUrl shouldBe clientServiceUrl
        sdkContext.defaultUrls.predictBaseUrl shouldBe predictServiceUrl
        sdkContext.remoteLogLevel shouldBe LogLevel.Error
        sdkContext.features shouldBe listOf(Features.MOBILE_ENGAGE)
    }

    @Test
    fun testHandleGlobalConfig() = runTest {
        val clientServiceUrl = "testClientServiceUrl"
        val predictServiceUrl = "testPredictServiceUrl"
        val clientId = "testClientId"
        val config = RemoteConfigResponse(
            ServiceUrls(
                clientService = clientServiceUrl
            ), LogLevel.Debug,
            LuckyLogger(LogLevel.Error, 1.0),
            RemoteConfigFeatures(mobileEngage = true),
            overrides = mapOf(
                clientId to RemoteConfig(
                    ServiceUrls(predictService = predictServiceUrl)
                ),
                "differentClientId" to RemoteConfig(
                    ServiceUrls(clientService = "differentClientServiceUrl")
                )
            )
        )

        everySuspend { mockDeviceInfoCollector.getClientId() } returns clientId
        everySuspend { mockRemoteConfigClient.fetchRemoteConfig(any()) } returns config
        every { mockRandomProvider.provide() } returns 0.1

        remoteConfigHandler.handleGlobal()

        sdkContext.defaultUrls.clientServiceBaseUrl shouldBe clientServiceUrl
        sdkContext.defaultUrls.predictBaseUrl shouldBe predictServiceUrl
        sdkContext.remoteLogLevel shouldBe LogLevel.Error
        sdkContext.features shouldBe listOf(Features.MOBILE_ENGAGE)
    }

}