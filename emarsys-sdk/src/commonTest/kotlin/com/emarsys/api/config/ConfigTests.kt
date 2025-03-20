package com.emarsys.api.config

import com.emarsys.EmarsysConfig
import com.emarsys.SdkConstants
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
        const val CLIENT_ID = "testClientId"
        const val LANGUAGE_CODE = "testLanguageCode"
        const val SDK_VERSION = "testSdkVersion"
        val PUSH_SETTINGS =
            AndroidNotificationSettings(true, 1, listOf(ChannelSettings("testChannelId")))
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
    }

    private lateinit var mockDeviceInfoCollector: DeviceInfoCollectorApi

    private lateinit var mockLoggingConfig: ConfigInstance

    private lateinit var mockGathererConfig: ConfigInstance

    private lateinit var mockInternalConfig: ConfigInstance

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


        everySuspend { mockDeviceInfoCollector.collect() } returns Json.encodeToString(DEVICE_INFO)
        everySuspend { mockDeviceInfoCollector.getPushSettings() } returns PUSH_SETTINGS
        everySuspend { mockDeviceInfoCollector.getClientId() } returns CLIENT_ID

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
        config.getContactFieldId() shouldBe CONTACT_FIELD_ID
    }

    @Test
    fun testApplicationCode_returnsCorrectValue() = runTest {
        config.getApplicationCode() shouldBe APPLICATION_CODE
    }

    @Test
    fun testMerchantId_returnsCorrectValue() = runTest {
        config.getMerchantId() shouldBe MERCHANT_ID
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
    fun testPushSettings_returnsCorrectValue() = runTest {
        config.getPushSettings() shouldBe PUSH_SETTINGS
    }

    @Test
    fun testSdkVersion_returnsCorrectValue() = runTest {
        config.getSdkVersion() shouldBe SDK_VERSION
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