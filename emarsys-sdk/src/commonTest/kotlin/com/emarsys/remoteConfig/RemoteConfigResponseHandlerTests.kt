package com.emarsys.remoteConfig

import com.emarsys.context.DefaultUrls
import com.emarsys.context.Features
import com.emarsys.context.SdkContext
import com.emarsys.context.SdkContextApi
import com.emarsys.core.device.DeviceInfoCollectorApi
import com.emarsys.core.log.LogLevel
import com.emarsys.core.log.SdkLogger
import com.emarsys.core.providers.DoubleProvider
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.di.SdkKoinIsolationContext.koin
import com.emarsys.fake.FakeStringStorage
import com.emarsys.util.JsonUtil
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.koin.core.Koin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.test.KoinTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class RemoteConfigResponseHandlerTests: KoinTest {

    override fun getKoin(): Koin = koin

    private lateinit var testModule: Module

    private lateinit var sdkContext: SdkContextApi
    private lateinit var mockDeviceInfoCollector: DeviceInfoCollectorApi
    private lateinit var mockRandomProvider: DoubleProvider
    private lateinit var remoteConfigResponseHandler: RemoteConfigResponseHandler

    @BeforeTest
    fun setUp() {
        testModule = module {
            single<StringStorageApi> { FakeStringStorage() }
            single<Json> { JsonUtil.json }
        }
        koin.loadModules(listOf(testModule))

        sdkContext = SdkContext(
            StandardTestDispatcher(),
            StandardTestDispatcher(),
            DefaultUrls("", "", "", "", "", "", ""),
            LogLevel.Debug,
            mutableSetOf(),
            logBreadcrumbsQueueSize = 10
        )
        mockDeviceInfoCollector = mock()
        mockRandomProvider = mock()

        remoteConfigResponseHandler = RemoteConfigResponseHandler(
            mockDeviceInfoCollector,
            sdkContext,
            mockRandomProvider,
            SdkLogger("TestLoggerName", mock(MockMode.autofill), sdkContext = sdkContext)
        )
    }

    @AfterTest
    fun tearDown() {
        koin.unloadModules(listOf(testModule))
    }

    @Test
    fun testHandleAppCodeBasedConfigs() = runTest {
        val clientServiceUrl = "testClientServiceUrl"
        val predictServiceUrl = "testPredictServiceUrl"
        val clientId = "testClientId"
        val configResponse = RemoteConfigResponse(
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
        every { mockRandomProvider.provide() } returns 0.1

        remoteConfigResponseHandler.handle(configResponse)

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
        val configResponse = RemoteConfigResponse(
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
        every { mockRandomProvider.provide() } returns 0.1

        remoteConfigResponseHandler.handle(configResponse)

        sdkContext.defaultUrls.clientServiceBaseUrl shouldBe clientServiceUrl
        sdkContext.defaultUrls.predictBaseUrl shouldBe predictServiceUrl
        sdkContext.remoteLogLevel shouldBe LogLevel.Error
        sdkContext.features shouldBe listOf(Features.MOBILE_ENGAGE)
    }

}