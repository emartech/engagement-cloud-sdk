package com.sap.ec.api.setup

import com.sap.ec.api.config.JsApiConfig
import com.sap.ec.api.config.ServiceWorkerOptions
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verifySuspend
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.js.Promise
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class JsSetupTests {
    private companion object {
        const val APPLICATION_CODE = "test-app-code"
        val testException = RuntimeException("test exception")
        val testSuccessResult = Result.success(Unit)
        val testFailureResult = Result.failure<Unit>(testException)
    }

    private lateinit var jsSetup: JsSetup
    private lateinit var mockSetupApi: SetupApi

    @BeforeTest
    fun setup() {
        mockSetupApi = mock(MockMode.autofill)
        jsSetup = JsSetup(mockSetupApi)
    }

    @Test
    fun enable_shouldDelegate_toSetupApiEnable() = runTest {
        val config = JsApiConfig(applicationCode = APPLICATION_CODE)
        val onContactLinkingFailed: () -> Promise<JsLinkContactData?> = { Promise.resolve(null) }
        everySuspend { mockSetupApi.enable(any(), any()) } returns testSuccessResult

        jsSetup.enable(config, onContactLinkingFailed)

        verifySuspend { mockSetupApi.enable(any(), any()) }
    }

    @Test
    fun enable_shouldDelegate_toSetupApiEnable_withServiceWorkerOptions() = runTest {
        val serviceWorkerOptions = ServiceWorkerOptions(
            applicationServerKey = "test-key",
            serviceWorkerPath = "/sw.js",
            serviceWorkerScope = "/"
        )
        val config = JsApiConfig(
            applicationCode = APPLICATION_CODE,
            serviceWorkerOptions = serviceWorkerOptions
        )
        val onContactLinkingFailed: () -> Promise<JsLinkContactData?> = { Promise.resolve(null) }
        everySuspend { mockSetupApi.enable(any(), any()) } returns testSuccessResult

        jsSetup.enable(config, onContactLinkingFailed)

        verifySuspend { mockSetupApi.enable(any(), any()) }
    }

    @Test
    fun enable_shouldThrow_whenSetupApiReturnsFailure() = runTest {
        val config = JsApiConfig(applicationCode = APPLICATION_CODE)
        val onContactLinkingFailed: () -> Promise<JsLinkContactData?> = { Promise.resolve(null) }
        everySuspend { mockSetupApi.enable(any(), any()) } returns testFailureResult

        shouldThrow<RuntimeException> {
            jsSetup.enable(config, onContactLinkingFailed)
        }
    }

    @Test
    fun disable_shouldDelegate_toSetupApiDisable() = runTest {
        everySuspend { mockSetupApi.disable() } returns testSuccessResult

        jsSetup.disable()

        verifySuspend { mockSetupApi.disable() }
    }

    @Test
    fun disable_shouldThrow_whenSetupApiReturnsFailure() = runTest {
        everySuspend { mockSetupApi.disable() } returns testFailureResult

        shouldThrow<RuntimeException> {
            jsSetup.disable()
        }
    }

    @Test
    fun isEnabled_shouldDelegate_toSetupApiIsEnabled_andReturnTrue() = runTest {
        everySuspend { mockSetupApi.isEnabled() } returns true

        val result = jsSetup.isEnabled()

        result shouldBe true
        verifySuspend { mockSetupApi.isEnabled() }
    }

    @Test
    fun isEnabled_shouldDelegate_toSetupApiIsEnabled_andReturnFalse() = runTest {
        everySuspend { mockSetupApi.isEnabled() } returns false

        val result = jsSetup.isEnabled()

        result shouldBe false
        verifySuspend { mockSetupApi.isEnabled() }
    }

    @Test
    fun setOnContactLinkingFailedCallback_shouldDelegate_toSetupApi() = runTest {
        val onContactLinkingFailed: () -> Promise<JsLinkContactData?> = { Promise.resolve(null) }

        jsSetup.setOnContactLinkingFailedCallback(onContactLinkingFailed)

        verify { mockSetupApi.setOnContactLinkingFailedCallback(any()) }
    }
}
