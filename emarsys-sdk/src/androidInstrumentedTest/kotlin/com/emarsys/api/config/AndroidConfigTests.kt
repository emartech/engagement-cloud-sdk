package com.emarsys.api.config

import AndroidConfig
import AndroidConfigApi
import com.emarsys.core.device.notification.AndroidNotificationSettings
import com.emarsys.core.device.notification.AndroidNotificationSettingsCollectorApi
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test


class AndroidConfigTests {
    private companion object {
        val testAndroidNotificationSettings = AndroidNotificationSettings(true, 1000, emptyList())
    }

    private lateinit var mockConfigApi: ConfigApi
    private lateinit var mockAndroidNotificationSettingsCollector: AndroidNotificationSettingsCollectorApi
    private lateinit var androidConfig: AndroidConfigApi

    @Before
    fun setup() {
        mockConfigApi = mock(MockMode.autofill)

        mockAndroidNotificationSettingsCollector = mock(MockMode.autofill)

        androidConfig = AndroidConfig(mockConfigApi, mockAndroidNotificationSettingsCollector)
    }

    @Test
    fun getApplicationCode_shouldCall_sameMethod_onConfigApi() = runTest {
        androidConfig.getApplicationCode()

        verifySuspend { mockConfigApi.getApplicationCode() }
    }

    @Test
    fun getClientId_shouldCall_sameMethod_onConfigApi() = runTest {
        androidConfig.getClientId()

        verifySuspend { mockConfigApi.getClientId() }
    }

    @Test
    fun getLanguageCode_shouldCall_sameMethod_onConfigApi() = runTest {
        androidConfig.getLanguageCode()

        verifySuspend { mockConfigApi.getLanguageCode() }
    }

    @Test
    fun getApplicationVersion_shouldCall_sameMethod_onConfigApi() = runTest {
        androidConfig.getApplicationVersion()

        verifySuspend { mockConfigApi.getApplicationVersion() }
    }

    @Test
    fun getSdkVersion_shouldCall_sameMethod_onConfigApi() = runTest {
        androidConfig.getSdkVersion()

        verifySuspend { mockConfigApi.getSdkVersion() }
    }

    @Test
    fun changeApplicationCode_shouldCall_sameMethod_onConfigApi_andReturnSuccessResult() = runTest {
        val testAppCode = "ABCDE-12345"
        everySuspend { mockConfigApi.changeApplicationCode(testAppCode) } returns Result.success(Unit)

        val result = androidConfig.changeApplicationCode(testAppCode)

        verifySuspend { mockConfigApi.changeApplicationCode(testAppCode) }
        result shouldBe Result.success(Unit)
    }

    @Test
    fun changeApplicationCode_shouldCall_sameMethod_onConfigApi_andReturnFailure_Result() =
        runTest {
            val testAppCode = "ABCDE-12345"
            val testFailure = Result.failure<Unit>(Exception("failure"))
            everySuspend { mockConfigApi.changeApplicationCode(testAppCode) } returns testFailure

            val result = androidConfig.changeApplicationCode(testAppCode)

            verifySuspend { mockConfigApi.changeApplicationCode(testAppCode) }
            result shouldBe testFailure
        }

    @Test
    fun setLanguage_shouldCall_sameMethod_onConfigApi_andReturnSuccess_Result() = runTest {
        val testLanguage = "en-GB"
        everySuspend { mockConfigApi.setLanguage(testLanguage) } returns Result.success(Unit)

        val result = androidConfig.setLanguage(testLanguage)

        verifySuspend { mockConfigApi.setLanguage(testLanguage) }
        result shouldBe Result.success(Unit)
    }

    @Test
    fun setLanguage_shouldCall_sameMethod_onConfigApi_andReturnFailure_Result() = runTest {
        val testLanguage = "en-GB"
        val testFailure = Result.failure<Unit>(Exception("failure"))
        everySuspend { mockConfigApi.setLanguage(testLanguage) } returns testFailure

        val result = androidConfig.setLanguage(testLanguage)

        verifySuspend { mockConfigApi.setLanguage(testLanguage) }
        result shouldBe testFailure
    }

    @Test
    fun resetLanguage_shouldCall_sameMethod_onConfigApi_andReturnSuccess_Result() = runTest {
        androidConfig.resetLanguage()

        verifySuspend { mockConfigApi.resetLanguage() }
    }

    @Test
    fun resetLanguage_shouldCall_sameMethod_onConfigApi_andReturnFailure_Result() = runTest {
        val testException = Exception("failure")
        val testFailure = Result.failure<Unit>(testException)
        everySuspend { mockConfigApi.resetLanguage() } returns testFailure

        val result = androidConfig.resetLanguage()

        verifySuspend { mockConfigApi.resetLanguage() }
        result shouldBe testFailure
    }

    @Test
    fun getNotificationSettings_shouldCall_sameMethod_onAndroidNotificationSettingsCollector() =
        runTest {
            everySuspend { mockAndroidNotificationSettingsCollector.collect() } returns testAndroidNotificationSettings

            val settings = androidConfig.getNotificationSettings()

            verifySuspend { mockAndroidNotificationSettingsCollector.collect() }

            settings shouldBe testAndroidNotificationSettings
        }
}