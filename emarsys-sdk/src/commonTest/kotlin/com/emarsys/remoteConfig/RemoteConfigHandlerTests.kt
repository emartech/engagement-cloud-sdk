package com.emarsys.remoteConfig

import com.emarsys.context.DefaultUrls
import com.emarsys.context.Features
import com.emarsys.context.SdkContext
import com.emarsys.core.device.DeviceInfoCollectorApi
import com.emarsys.core.log.LogLevel
import com.emarsys.networking.clients.remoteConfig.RemoteConfigClientApi
import com.emarsys.core.providers.Provider
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.Test

class RemoteConfigHandlerTests : TestsWithMocks() {

    override fun setUpMocks() = injectMocks(mocker)

    @Mock
    lateinit var mockRemoteConfigClient: RemoteConfigClientApi

    @Mock
    lateinit var mockDeviceInfoCollector: DeviceInfoCollectorApi

    private val sdkContext = SdkContext(StandardTestDispatcher(), DefaultUrls("", "", "", "", "", "", ""), LogLevel.Debug, mutableSetOf())

    @Mock
    lateinit var mockRandomProvider: Provider<Double>

    private var remoteConfigHandler: RemoteConfigHandler by withMocks {
        RemoteConfigHandler(
            mockRemoteConfigClient,
            mockDeviceInfoCollector,
            sdkContext,
            mockRandomProvider
        )
    }

    @Test
    fun testHandle() = runTest {
        val clientServiceUrl = "testClientServiceUrl"
        val predictServiceUrl = "testPredictServiceUrl"
        val hardwareId = "testHardwareId"
        val config = RemoteConfigResponse(
            ServiceUrls(
                clientService = clientServiceUrl
            ), LogLevel.Debug,
            LuckyLogger(LogLevel.Error, 1.0),
            RemoteConfigFeatures(mobileEngage = true),
            overrides = mapOf(
                hardwareId to RemoteConfig(
                    ServiceUrls(predictService = predictServiceUrl)
                ),
                "differentHardwareId" to RemoteConfig(
                    ServiceUrls(clientService = "differentClientServiceUrl")
                )
            )
        )

        every { mockDeviceInfoCollector.getHardwareId() } returns hardwareId
        everySuspending { mockRemoteConfigClient.fetchRemoteConfig() } returns config
        every { mockRandomProvider.provide() } returns 0.1

        remoteConfigHandler.handle()

        sdkContext.defaultUrls.clientServiceBaseUrl shouldBe clientServiceUrl
        sdkContext.defaultUrls.predictBaseUrl shouldBe predictServiceUrl
        sdkContext.remoteLogLevel shouldBe LogLevel.Error
        sdkContext.features shouldBe listOf(Features.MOBILE_ENGAGE)
    }

}