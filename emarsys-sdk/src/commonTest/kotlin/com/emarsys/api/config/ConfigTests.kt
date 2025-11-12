package com.emarsys.api.config

import com.emarsys.SdkConstants
import com.emarsys.TestEmarsysConfig
import com.emarsys.api.SdkState
import com.emarsys.context.DefaultUrls
import com.emarsys.context.SdkContext
import com.emarsys.context.SdkContextApi
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.device.DeviceInfoCollectorApi
import com.emarsys.core.device.NotificationSettings
import com.emarsys.core.log.LogLevel
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.di.SdkKoinIsolationContext.koin
import com.emarsys.fake.FakeStringStorage
import com.emarsys.util.JsonUtil
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
import org.koin.core.Koin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.test.KoinTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConfigTest : KoinTest {

    override fun getKoin(): Koin = koin

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
    }

    private lateinit var testModule: Module

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
        testModule = module {
            single<StringStorageApi> { FakeStringStorage() }
            single<Json> { JsonUtil.json }
        }
        koin.loadModules(listOf(testModule))

        mockDeviceInfoCollector = mock()
        mockLoggingConfig = mock()
        mockGathererConfig = mock()
        mockInternalConfig = mock()

        sdkContext = SdkContext(
            StandardTestDispatcher(),
            mainDispatcher,
            DefaultUrls("", "", "", "", "", ""),
            LogLevel.Error,
            mutableSetOf(),
            logBreadcrumbsQueueSize = 10
        )
        sdkContext.config = TestEmarsysConfig(APPLICATION_CODE)

        everySuspend { mockDeviceInfoCollector.collect() } returns Json.encodeToString(DEVICE_INFO)
        everySuspend { mockDeviceInfoCollector.getNotificationSettings() } returns PUSH_SETTINGS
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
        koin.unloadModules(listOf(testModule))
    }

    @Test
    fun testApplicationCode_returnsCorrectValue() = runTest {
        config.getApplicationCode() shouldBe APPLICATION_CODE
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
        sdkContext.setSdkState(SdkState.Inactive)

        config.getCurrentSdkState() shouldBe SdkState.Inactive

        sdkContext.setSdkState(SdkState.Active)
        config.getCurrentSdkState() shouldBe SdkState.Active
    }

    @Test
    fun testChangeApplicationCode_delegatesToCorrectInstance() = runTest {
        val newAppCode = "newAppCode"
        everySuspend { mockGathererConfig.changeApplicationCode(newAppCode) } returns Unit

        sdkContext.setSdkState(SdkState.OnHold)
        config.changeApplicationCode(newAppCode)

        verifySuspend {
            mockGathererConfig.changeApplicationCode(newAppCode)
        }
    }

    @Test
    fun testResetLanguage_delegatesToCorrectInstance() = runTest {
        everySuspend { mockGathererConfig.resetLanguage() } returns Unit

        sdkContext.setSdkState(SdkState.OnHold)
        config.resetLanguage()

        verifySuspend {
            mockGathererConfig.resetLanguage()
        }
    }
}