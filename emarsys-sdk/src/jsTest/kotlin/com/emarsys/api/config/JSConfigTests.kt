package com.emarsys.api.config

import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.assertions.throwables.shouldThrow
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
    private lateinit var jSConfig: JSConfigApi


    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        mockConfigApi = mock(MockMode.autofill)
        jSConfig = JSConfig(mockConfigApi, TestScope())
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun getContactFieldId_shouldCall_getContactFieldId_onConfigApi() = runTest {
        jSConfig.getContactFieldId().await()

        verifySuspend { mockConfigApi.getContactFieldId() }
    }

    @Test
    fun getApplicationCode_shouldCall_getApplicationCode_onConfigApi() = runTest {
        jSConfig.getApplicationCode().await()

        verifySuspend { mockConfigApi.getApplicationCode() }
    }

    @Test
    fun getMerchantId_shouldCall_getMerchantId_onConfigApi() = runTest {
        jSConfig.getMerchantId().await()

        verifySuspend { mockConfigApi.getMerchantId() }
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
    fun getPushSettings_shouldCall_getPushSettings_onConfigApi() = runTest {
        jSConfig.getPushSettings().await()

        verifySuspend { mockConfigApi.getPushSettings() }
    }

    @Test
    fun changeApplicationCode_shouldCall_changeApplicationCode_onConfigApi() = runTest {
        val testAppCode = "testAppCode"
        val mockConfigApi: ConfigApi = mock(MockMode.autofill)
        everySuspend { mockConfigApi.changeApplicationCode(testAppCode) } returns Result.success(Unit)

        jSConfig = JSConfig(mockConfigApi, TestScope())

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

        val jSConfig = JSConfig(mockConfigApi, TestScope())

        shouldThrow<CancellationException> {  jSConfig.changeApplicationCode(testAppCode).await() }
    }

    @Test
    fun changeMerchantId_shouldCall_changeMerchantId_onConfigApi() = runTest {
        val testMerchantId = "testMerchantId"
        val mockConfigApi: ConfigApi = mock(MockMode.autofill)
        everySuspend { mockConfigApi.changeMerchantId(testMerchantId) } returns Result.success(Unit)

        jSConfig = JSConfig(mockConfigApi, TestScope())

        jSConfig.changeMerchantId(testMerchantId).await()

        verifySuspend { mockConfigApi.changeMerchantId(testMerchantId) }
    }

    @Test
    fun changeMerchantId_shouldThrowException_ifChangeMerchantId_failed() = runTest {
        val testMerchantId = "testMerchantId"
        val mockConfigApi: ConfigApi = mock(MockMode.autofill)
        everySuspend { mockConfigApi.changeMerchantId(testMerchantId) } returns testFailureResult
        val jSConfig = JSConfig(mockConfigApi, TestScope())

        shouldThrow<Exception> {  jSConfig.changeMerchantId(testMerchantId).await() }
    }

    @Test
    fun setLanguage_shouldCall_setLanguage_onConfigApi() = runTest {
        val testLanguage = "testLanguage"
        val mockConfigApi: ConfigApi = mock(MockMode.autofill)
        everySuspend { mockConfigApi.setLanguage(testLanguage) } returns Result.success(Unit)

        jSConfig = JSConfig(mockConfigApi, TestScope())

        jSConfig.setLanguage(testLanguage).await()

        verifySuspend { mockConfigApi.setLanguage(testLanguage) }
    }

    @Test
    fun setLanguage_shouldThrowException_ifSetLanguage_failed() = runTest {
        val testLanguage = "testLanguage"
        val mockConfigApi: ConfigApi = mock(MockMode.autofill)
        everySuspend { mockConfigApi.setLanguage(testLanguage) } returns testFailureResult
        val jSConfig = JSConfig(mockConfigApi, TestScope())

        shouldThrow<Exception> {  jSConfig.setLanguage(testLanguage).await() }
    }

    @Test
    fun resetLanguage_shouldCall_resetLanguage_onConfigApi() = runTest {
        val mockConfigApi: ConfigApi = mock(MockMode.autofill)
        everySuspend { mockConfigApi.resetLanguage() } returns Result.success(Unit)

        jSConfig = JSConfig(mockConfigApi, TestScope())

        jSConfig.resetLanguage().await()

        verifySuspend { mockConfigApi.resetLanguage() }
    }

    @Test
    fun resetLanguage_shouldThrowException_ifResetLanguage_failed() = runTest {
        val mockConfigApi: ConfigApi = mock(MockMode.autofill)
        everySuspend { mockConfigApi.resetLanguage() } returns testFailureResult
        val jSConfig = JSConfig(mockConfigApi, TestScope())

        shouldThrow<Exception> {  jSConfig.resetLanguage().await() }
    }
}