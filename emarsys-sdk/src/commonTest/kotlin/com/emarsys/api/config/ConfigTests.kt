package com.emarsys.api.config

import com.emarsys.EmarsysConfig
import com.emarsys.api.SdkResult
import com.emarsys.api.SdkState
import com.emarsys.context.DefaultUrls
import com.emarsys.context.SdkContext
import com.emarsys.context.SdkContextApi
import com.emarsys.core.device.AndroidNotificationSettings
import com.emarsys.core.device.ChannelSettings
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.device.DeviceInfoCollectorApi
import com.emarsys.core.log.LogLevel
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConfigTests : TestsWithMocks() {
    override fun setUpMocks() = injectMocks(mocker)

    private companion object {
        const val CONTACT_FIELD_ID = 42
        const val APPLICATION_CODE = "testApplicationCode"
        const val MERCHANT_ID = "testMerchantId"
        const val HW_ID = "testHwId"
        const val LANGUAGE_CODE = "testLanguageCode"
        const val SDK_VERSION = "testSdkVersion"
        val PUSH_SETTINGS =
            AndroidNotificationSettings(true, 1, listOf(ChannelSettings("testChannelId")))
        val DEVICE_INFO = DeviceInfo(
            "testPlatform",
            "testAppVersion",
            "testDeviceModel",
            "testOsVersion",
            "testSdkVersion",
            "testLanguageCode",
            "testTimeZone"
        )
    }

    @Mock
    lateinit var deviceInfoCollector: DeviceInfoCollectorApi

    @Mock
    lateinit var mockLoggingConfig: ConfigInstance

    @Mock
    lateinit var mockGathererConfig: ConfigInstance

    @Mock
    lateinit var mockInternalConfig: ConfigInstance

    private lateinit var sdkContext: SdkContextApi

    private lateinit var config: Config<ConfigInstance, ConfigInstance, ConfigInstance>

    init {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @BeforeTest
    fun setup() = runTest {
        sdkContext = SdkContext(
            StandardTestDispatcher(),
            DefaultUrls("", "", "", "", "", "", ""),
            LogLevel.Error,
            mutableSetOf()
        )
        sdkContext.contactFieldId = CONTACT_FIELD_ID
        sdkContext.config = EmarsysConfig(APPLICATION_CODE, MERCHANT_ID)


        every { deviceInfoCollector.collect() } returns Json.encodeToString(DEVICE_INFO)
        every { deviceInfoCollector.getPushSettings() } returns PUSH_SETTINGS
        every { deviceInfoCollector.getHardwareId() } returns HW_ID

        everySuspending { mockLoggingConfig.activate() } returns Unit
        everySuspending { mockGathererConfig.activate() } returns Unit
        everySuspending { mockInternalConfig.activate() } returns Unit

        config = Config(
            mockLoggingConfig,
            mockGathererConfig,
            mockInternalConfig,
            sdkContext,
            deviceInfoCollector
        )
        config.registerOnContext()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        mocker.reset()
    }

    @Test
    fun testContactFieldId_returnsCorrectValue() = runTest {
        config.contactFieldId shouldBe CONTACT_FIELD_ID
    }

    @Test
    fun testApplicationCode_returnsCorrectValue() = runTest {
        config.applicationCode shouldBe APPLICATION_CODE
    }

    @Test
    fun testMerchantId_returnsCorrectValue() = runTest {
        config.merchantId shouldBe MERCHANT_ID
    }

    @Test
    fun testHardwareId_returnsCorrectValue() = runTest {
        config.hardwareId shouldBe HW_ID
    }

    @Test
    fun testLanguageCode_returnsCorrectValue() = runTest {
        config.languageCode shouldBe LANGUAGE_CODE
    }

    @Test
    fun testPushSettings_returnsCorrectValue() = runTest {
        config.pushSettings shouldBe PUSH_SETTINGS
    }

    @Test
    fun testSdkVersion_returnsCorrectValue() = runTest {
        config.sdkVersion shouldBe SDK_VERSION
    }

    @Test
    fun testChangeApplicationCode_delegatesToCorrectInstance() = runTest {
        val newAppCode = "newAppCode"
        everySuspending { mockGathererConfig.changeApplicationCode(newAppCode) } returns SdkResult.Success(Unit)

        sdkContext.setSdkState(SdkState.onHold)
        config.changeApplicationCode(newAppCode)

        verifyWithSuspend(exhaustive = false) {
            mockGathererConfig.changeApplicationCode(newAppCode)
        }
    }

    @Test
    fun testChangeMerchantId_delegatesToCorrectInstance() = runTest {
        val newMerchantId = "newMerchantId"
        everySuspending { mockInternalConfig.changeMerchantId(newMerchantId) } returns SdkResult.Success(Unit)

        sdkContext.setSdkState(SdkState.active)
        config.changeMerchantId(newMerchantId)

        verifyWithSuspend(exhaustive = false) {
            mockInternalConfig.changeMerchantId(newMerchantId)
        }
    }
}