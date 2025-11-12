package com.emarsys.api.config

import com.emarsys.api.SdkState
import com.emarsys.core.device.IosAlertStyle
import com.emarsys.core.device.IosAuthorizationStatus
import com.emarsys.core.device.IosNotificationSetting
import com.emarsys.core.device.IosShowPreviewSetting
import com.emarsys.core.device.notification.IosNotificationSettings
import com.emarsys.core.device.notification.IosNotificationSettingsCollectorApi
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class IosConfigTests {
    private companion object {
        val iosNotificationSettings = IosNotificationSettings(
            IosAuthorizationStatus.Authorized,
            IosNotificationSetting.Enabled,
            IosNotificationSetting.Enabled,
            IosNotificationSetting.Enabled,
            IosNotificationSetting.Enabled,
            IosNotificationSetting.Enabled,
            IosNotificationSetting.Enabled,
            IosAlertStyle.Alert,
            IosShowPreviewSetting.Always,
            IosNotificationSetting.Enabled,
            false,
            IosNotificationSetting.Enabled,
            IosNotificationSetting.Enabled
        )
        val successResult = Result.success(Unit)
        val testException = Exception("failure")
        val failureResult = Result.failure<Unit>(testException)
    }

    private lateinit var mockConfigApi: ConfigApi
    private lateinit var mockIosNotificationSettingsCollector: IosNotificationSettingsCollectorApi
    private lateinit var iosConfig: IosConfigApi

    @BeforeTest
    fun setup() {
        mockConfigApi = mock(MockMode.autofill)

        mockIosNotificationSettingsCollector = mock(MockMode.autofill)

        iosConfig = IosConfig(mockConfigApi, mockIosNotificationSettingsCollector)
    }


    @Test
    fun getApplicationCode_shouldCall_sameMethod_onConfigApi() = runTest {
        iosConfig.getApplicationCode()

        verifySuspend { mockConfigApi.getApplicationCode() }
    }

    @Test
    fun getClientId_shouldCall_sameMethod_onConfigApi() = runTest {
        iosConfig.getClientId()

        verifySuspend { mockConfigApi.getClientId() }
    }

    @Test
    fun getLanguageCode_shouldCall_sameMethod_onConfigApi() = runTest {
        iosConfig.getLanguageCode()

        verifySuspend { mockConfigApi.getLanguageCode() }
    }

    @Test
    fun getApplicationVersion_shouldCall_sameMethod_onConfigApi() = runTest {
        iosConfig.getApplicationVersion()

        verifySuspend { mockConfigApi.getApplicationVersion() }
    }

    @Test
    fun getSdkVersion_shouldCall_sameMethod_onConfigApi() = runTest {
        iosConfig.getSdkVersion()

        verifySuspend { mockConfigApi.getSdkVersion() }
    }

    @Test
    fun getCurrentSdkState_shouldCall_sameMethod_onConfigApi() = runTest {
        everySuspend { mockConfigApi.getCurrentSdkState() } returns SdkState.Active

        iosConfig.getCurrentSdkState()

        verifySuspend { mockConfigApi.getCurrentSdkState() }
    }

    @Test
    fun changeApplicationCode_shouldCall_sameMethod_onConfigApi_andReturn_unit() = runTest {
        val testAppCode = "ABCDE-12345"
        everySuspend { mockConfigApi.changeApplicationCode(testAppCode) } returns successResult

        val result = iosConfig.changeApplicationCode(testAppCode)

        verifySuspend { mockConfigApi.changeApplicationCode(testAppCode) }
        result shouldBe Unit
    }

    @Test
    fun changeApplicationCode_shouldCall_sameMethod_onConfigApi_andThrowException() =
        runTest {
            val testAppCode = "ABCDE-12345"
            everySuspend { mockConfigApi.changeApplicationCode(testAppCode) } returns failureResult

            val exception = shouldThrow<Exception> { iosConfig.changeApplicationCode(testAppCode) }

            verifySuspend { mockConfigApi.changeApplicationCode(testAppCode) }
            exception shouldBe testException
        }

    @Test
    fun setLanguage_shouldCall_sameMethod_onConfigApi_andReturnSuccess_unit() = runTest {
        val testLanguage = "en-GB"
        everySuspend { mockConfigApi.setLanguage(testLanguage) } returns successResult

        val result = iosConfig.setLanguage(testLanguage)

        verifySuspend { mockConfigApi.setLanguage(testLanguage) }
        result shouldBe Unit
    }

    @Test
    fun setLanguage_shouldCall_sameMethod_onConfigApi_andThrowException() = runTest {
        val testLanguage = "en-GB"
        val testFailure = Result.failure<Unit>(Exception("failure"))
        everySuspend { mockConfigApi.setLanguage(testLanguage) } returns testFailure

        val exception = shouldThrow<Exception> { iosConfig.setLanguage(testLanguage) }

        verifySuspend { mockConfigApi.setLanguage(testLanguage) }
        exception shouldBe testException
    }

    @Test
    fun resetLanguage_shouldCall_sameMethod_onConfigApi_andReturnSuccess_unit() = runTest {
        everySuspend { mockConfigApi.resetLanguage() } returns successResult

        iosConfig.resetLanguage()

        verifySuspend { mockConfigApi.resetLanguage() }
    }

    @Test
    fun resetLanguage_shouldCall_sameMethod_onConfigApi_andReturnFailure_Result() = runTest {
        everySuspend { mockConfigApi.resetLanguage() } returns failureResult

        val exception = shouldThrow<Exception> { iosConfig.resetLanguage() }

        verifySuspend { mockConfigApi.resetLanguage() }
        exception shouldBe testException
    }

    @Test
    fun getNotificationSettings_shouldCall_sameMethod_onAndroidNotificationSettingsCollector() =
        runTest {
            everySuspend { mockIosNotificationSettingsCollector.collect() } returns iosNotificationSettings

            val settings = iosConfig.getNotificationSettings()

            verifySuspend { mockIosNotificationSettingsCollector.collect() }

            settings shouldBe iosNotificationSettings
        }

}