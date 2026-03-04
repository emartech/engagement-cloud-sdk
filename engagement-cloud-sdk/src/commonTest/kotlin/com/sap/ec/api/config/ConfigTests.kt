package com.sap.ec.api.config

import com.sap.ec.SdkConstants
import com.sap.ec.TestEngagementCloudSDKConfig
import com.sap.ec.api.SdkState
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.device.DeviceInfo
import com.sap.ec.core.device.DeviceInfoCollectorApi
import com.sap.ec.core.device.NotificationSettings
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.answering.sequentially
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConfigTest {
    private companion object {
        const val APPLICATION_CODE = "testApplicationCode"
        const val CLIENT_ID = "testClientId"
        const val LANGUAGE_CODE = "testLanguageCode"
        const val SDK_VERSION = "testSdkVersion"
        val PUSH_SETTINGS =
            NotificationSettings(true)
        val DEVICE_INFO = DeviceInfo(
            "testPlatform",
            SdkConstants.WEB_PLATFORM_CATEGORY,
            null,
            null,
            "testAppVersion",
            "testDeviceModel",
            "testOsVersion",
            "testSdkVersion",
            "testLanguageCode",
            "testTimeZone",
            "testClientId"
        )
        val TEST_CONFIG = TestEngagementCloudSDKConfig(APPLICATION_CODE)
    }

    private lateinit var mockDeviceInfoCollector: DeviceInfoCollectorApi

    private lateinit var mockLoggingConfig: ConfigInstance

    private lateinit var mockGathererConfig: ConfigInstance

    private lateinit var mockInternalConfig: ConfigInstance

    private lateinit var mockSdkContext: SdkContextApi

    private lateinit var config: Config<ConfigInstance, ConfigInstance, ConfigInstance>

    private val mainDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(mainDispatcher)
        mockDeviceInfoCollector = mock(MockMode.autofill)
        mockLoggingConfig = mock(MockMode.autofill)
        mockGathererConfig = mock(MockMode.autofill)
        mockInternalConfig = mock(MockMode.autofill)
        mockSdkContext = mock(MockMode.autofill)

        every { mockSdkContext.config } returns TEST_CONFIG
        every { mockSdkContext.sdkDispatcher } returns StandardTestDispatcher()

        everySuspend { mockDeviceInfoCollector.collect() } returns Json.encodeToString(DEVICE_INFO)
        everySuspend { mockDeviceInfoCollector.collectAsDeviceInfo() } returns DEVICE_INFO
        everySuspend { mockDeviceInfoCollector.getNotificationSettings() } returns PUSH_SETTINGS
        everySuspend { mockDeviceInfoCollector.getClientId() } returns CLIENT_ID

        everySuspend { mockLoggingConfig.activate() } returns Unit
        everySuspend { mockGathererConfig.activate() } returns Unit
        everySuspend { mockInternalConfig.activate() } returns Unit

        config = Config(
            mockLoggingConfig,
            mockGathererConfig,
            mockInternalConfig,
            mockSdkContext,
            mockDeviceInfoCollector
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testApplicationCode_whenActiveState_returnsApplicationCode() = runTest {
        every { mockSdkContext.config } returns TEST_CONFIG
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Active)

        config.registerOnContext()

        config.getApplicationCode() shouldBe APPLICATION_CODE
    }

    @Test
    fun testApplicationCode_whenOnHoldState_awaitsAndReturnsApplicationCodeWhenSdkBecomesActive() =
        runTest {
            every { mockSdkContext.currentSdkState } sequentially {
                returns(MutableStateFlow(SdkState.OnHold))
                returns(MutableStateFlow(SdkState.Active))
            }

            config.registerOnContext()

            val result = async { config.getApplicationCode() }

            advanceUntilIdle()

            result.await() shouldBe APPLICATION_CODE
        }

    @Test
    fun testApplicationCode_whenOnHoldState_awaitsAndReturnsApplicationCodeWhenSdkBecomesInitialized() =
        runTest {
            every { mockSdkContext.currentSdkState } sequentially {
                returns(MutableStateFlow(SdkState.OnHold))
                returns(MutableStateFlow(SdkState.Initialized))
            }
            every { mockSdkContext.config } returns null

            config.registerOnContext()

            val result = async {
                config.getApplicationCode()
            }

            advanceUntilIdle()

            result.await() shouldBe null
        }

    @Test
    fun testApplicationCode_whenUnInitializedState_returnsNull() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.UnInitialized)

        config.registerOnContext()

        config.getApplicationCode() shouldBe null
    }

    @Test
    fun testApplicationCode_whenInitializedState_returnsNull() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Initialized)

        config.registerOnContext()

        config.getApplicationCode() shouldBe null
    }

    @Test
    fun testClientId_returnsCorrectValue() = runTest {
        config.getClientId() shouldBe CLIENT_ID
    }

    @Test
    fun testLanguageCode_returnsCorrectValue() = runTest {
        config.getLanguageCode() shouldBe LANGUAGE_CODE
    }

    @Test
    fun testGetNotificationSettings_returnsCorrectValue() = runTest {
        config.getNotificationSettings() shouldBe PUSH_SETTINGS
    }

    @Test
    fun testSdkVersion_returnsCorrectValue() = runTest {
        config.getSdkVersion() shouldBe SDK_VERSION
    }

    @Test
    fun testCurrentSdkState_returnsCorrectValue() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.UnInitialized)

        config.getCurrentSdkState() shouldBe SdkState.UnInitialized
        verify { mockSdkContext.currentSdkState }
    }

    @Test
    fun testChangeApplicationCode_delegatesToCorrectInstance() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.OnHold)

        config.registerOnContext()
        val newAppCode = "newAppCode"
        everySuspend { mockGathererConfig.changeApplicationCode(newAppCode) } returns Unit

        config.changeApplicationCode(newAppCode)

        advanceUntilIdle()

        verifySuspend { mockGathererConfig.changeApplicationCode(newAppCode) }
    }

    @Test
    fun testResetLanguage_delegatesToCorrectInstance() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.OnHold)
        everySuspend { mockGathererConfig.resetLanguage() } returns Unit

        config.registerOnContext()

        config.resetLanguage()

        advanceUntilIdle()

        verifySuspend { mockGathererConfig.resetLanguage() }
    }
}