package com.emarsys.api.config

import com.emarsys.EmarsysConfig
import com.emarsys.api.SdkState
import com.emarsys.context.DefaultUrls
import com.emarsys.context.SdkContext
import com.emarsys.context.SdkContextApi
import com.emarsys.core.device.AndroidNotificationSettings
import com.emarsys.core.device.ChannelSettings
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.device.DeviceInfoCollectorApi
import com.emarsys.core.log.LogLevel
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConfigTest {

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

    lateinit var mockDeviceInfoCollector: DeviceInfoCollectorApi

    lateinit var mockLoggingConfig: ConfigInstance

    lateinit var mockGathererConfig: ConfigInstance

    lateinit var mockInternalConfig: ConfigInstance

    private lateinit var sdkContext: SdkContextApi

    private lateinit var config: Config<ConfigInstance, ConfigInstance, ConfigInstance>

    private val mainDispatcher = StandardTestDispatcher()

    init {
        Dispatchers.setMain(mainDispatcher)
    }

    @BeforeTest
    fun setup() = runTest {
        mockDeviceInfoCollector = mock()
        mockLoggingConfig = mock()
        mockGathererConfig = mock()
        mockInternalConfig = mock()

        sdkContext = SdkContext(
            StandardTestDispatcher(),
            mainDispatcher,
            DefaultUrls("", "", "", "", "", "", ""),
            LogLevel.Error,
            mutableSetOf()
        )
        sdkContext.contactFieldId = CONTACT_FIELD_ID
        sdkContext.config = EmarsysConfig(APPLICATION_CODE, MERCHANT_ID)


        every { mockDeviceInfoCollector.collect() } returns Json.encodeToString(DEVICE_INFO)
        every { mockDeviceInfoCollector.getPushSettings() } returns PUSH_SETTINGS
        every { mockDeviceInfoCollector.getHardwareId() } returns HW_ID

        everySuspend { mockLoggingConfig.activate() } returns Unit
        everySuspend { mockGathererConfig.activate() } returns Unit
        everySuspend { mockInternalConfig.activate() } returns Unit

        config = Config(
            mockLoggingConfig,
            mockGathererConfig,
            mockInternalConfig,
            sdkContext,
            mockDeviceInfoCollector
        )
        config.registerOnContext()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
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
        everySuspend { mockGathererConfig.changeApplicationCode(newAppCode) } returns Unit

        sdkContext.setSdkState(SdkState.onHold)
        config.changeApplicationCode(newAppCode)

        verifySuspend {
            mockGathererConfig.changeApplicationCode(newAppCode)
        }
    }

    @Test
    fun testChangeMerchantId_delegatesToCorrectInstance() = runTest {
        val newMerchantId = "newMerchantId"
        everySuspend { mockInternalConfig.changeMerchantId(newMerchantId) } returns Unit

        sdkContext.setSdkState(SdkState.active)
        config.changeMerchantId(newMerchantId)

        verifySuspend {
            mockInternalConfig.changeMerchantId(newMerchantId)
        }
    }
}