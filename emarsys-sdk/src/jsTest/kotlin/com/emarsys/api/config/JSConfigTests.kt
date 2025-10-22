package com.emarsys.api.config

import com.emarsys.core.device.notification.PermissionState
import com.emarsys.core.device.notification.WebNotificationSettings
import com.emarsys.core.device.notification.WebNotificationSettingsCollectorApi
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.await
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class JSConfigTests {
    private companion object {
        val testFailureResult = Result.failure<Unit>(Exception())
    }


    private lateinit var mockConfigApi: ConfigApi
    private lateinit var mockWebNotificationSettingsCollector: WebNotificationSettingsCollectorApi
    private lateinit var jSConfig: JSConfigApi


    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        mockConfigApi = mock(MockMode.autofill)
        mockWebNotificationSettingsCollector = mock(MockMode.autofill)
        jSConfig = JSConfig(mockConfigApi, mockWebNotificationSettingsCollector, TestScope())
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun getApplicationCode_shouldCall_getApplicationCode_onConfigApi() = runTest {
        jSConfig.getApplicationCode().await()

        verifySuspend { mockConfigApi.getApplicationCode() }
    }

    @Test
    fun getClientId_shouldCall_getClientId_onConfigApi() = runTest {
        jSConfig.getClientId().await()

        verifySuspend { mockConfigApi.getClientId() }
    }

    @Test
    fun getLanguageCode_shouldCall_getLanguageCode_onConfigApi() = runTest {
        jSConfig.getLanguageCode().await()

        verifySuspend { mockConfigApi.getLanguageCode() }
    }

    @Test
    fun getSdkVersion_shouldCall_getSdkVersion_onConfigApi() = runTest {
        jSConfig.getSdkVersion().await()

        verifySuspend { mockConfigApi.getSdkVersion() }
    }

    @Test
    fun getNotificationSettings_shouldCall_collect_onWebNotificationSettingsCollectorApi() =
        runTest {
            val testWebNotificationSettings = WebNotificationSettings(
                permissionState = PermissionState.Granted,
                isServiceWorkerRegistered = false,
                isSubscribed = false
            )
            everySuspend {
                mockWebNotificationSettingsCollector.collect()
            } returns testWebNotificationSettings

            val webNotificationSettings = jSConfig.getNotificationSettings().await()

            webNotificationSettings shouldBe testWebNotificationSettings
            verifySuspend { mockWebNotificationSettingsCollector.collect() }
        }

    @Test
    fun changeApplicationCode_shouldCall_changeApplicationCode_onConfigApi() = runTest {
        val testAppCode = "testAppCode"
        val mockConfigApi: ConfigApi = mock(MockMode.autofill)
        everySuspend { mockConfigApi.changeApplicationCode(testAppCode) } returns Result.success(
            Unit
        )

        jSConfig = JSConfig(mockConfigApi, mockWebNotificationSettingsCollector, TestScope())

        jSConfig.changeApplicationCode(testAppCode).await()

        verifySuspend { mockConfigApi.changeApplicationCode(testAppCode) }
    }

    @Test
    fun changeApplicationCode_shouldThrowException_ifChangeApplicationCode_failed() = runTest {
        val testAppCode = "testAppCode"
        val mockConfigApi: ConfigApi = mock(MockMode.autofill)
        everySuspend { mockConfigApi.changeApplicationCode(testAppCode) } returns Result.failure(
            CancellationException()
        )

        val jSConfig = JSConfig(mockConfigApi, mockWebNotificationSettingsCollector, TestScope())

        shouldThrow<CancellationException> { jSConfig.changeApplicationCode(testAppCode).await() }
    }

    @Test
    fun setLanguage_shouldCall_setLanguage_onConfigApi() = runTest {
        val testLanguage = "testLanguage"
        val mockConfigApi: ConfigApi = mock(MockMode.autofill)
        everySuspend { mockConfigApi.setLanguage(testLanguage) } returns Result.success(Unit)

        jSConfig = JSConfig(mockConfigApi, mockWebNotificationSettingsCollector, TestScope())

        jSConfig.setLanguage(testLanguage).await()

        verifySuspend { mockConfigApi.setLanguage(testLanguage) }
    }

    @Test
    fun setLanguage_shouldThrowException_ifSetLanguage_failed() = runTest {
        val testLanguage = "testLanguage"
        val mockConfigApi: ConfigApi = mock(MockMode.autofill)
        everySuspend { mockConfigApi.setLanguage(testLanguage) } returns testFailureResult
        val jSConfig = JSConfig(mockConfigApi, mockWebNotificationSettingsCollector, TestScope())

        shouldThrow<Exception> { jSConfig.setLanguage(testLanguage).await() }
    }

    @Test
    fun resetLanguage_shouldCall_resetLanguage_onConfigApi() = runTest {
        val mockConfigApi: ConfigApi = mock(MockMode.autofill)
        everySuspend { mockConfigApi.resetLanguage() } returns Result.success(Unit)

        jSConfig = JSConfig(mockConfigApi, mockWebNotificationSettingsCollector, TestScope())

        jSConfig.resetLanguage().await()

        verifySuspend { mockConfigApi.resetLanguage() }
    }

    @Test
    fun resetLanguage_shouldThrowException_ifResetLanguage_failed() = runTest {
        val mockConfigApi: ConfigApi = mock(MockMode.autofill)
        everySuspend { mockConfigApi.resetLanguage() } returns testFailureResult
        val jSConfig = JSConfig(mockConfigApi, mockWebNotificationSettingsCollector, TestScope())

        shouldThrow<Exception> { jSConfig.resetLanguage().await() }
    }
}