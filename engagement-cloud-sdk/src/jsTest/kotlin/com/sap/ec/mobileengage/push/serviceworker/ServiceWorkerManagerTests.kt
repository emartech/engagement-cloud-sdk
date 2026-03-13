package com.sap.ec.mobileengage.push.serviceworker

import JsEngagementCloudSDKConfig
import com.sap.ec.api.config.ServiceWorkerOptions
import com.sap.ec.context.SdkContextApi
import com.sap.ec.fake.FakeLogger
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.w3c.workers.ServiceWorkerRegistration
import web.serviceworker.ServiceWorkerContainer
import js.promise.Promise
import kotlin.js.unsafeCast
import kotlin.test.BeforeTest
import kotlin.test.Test

class ServiceWorkerManagerTests {

    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var fakeLogger: FakeLogger
    private lateinit var serviceWorkerManager: ServiceWorkerManager
    private lateinit var fakeServiceWorkerContainer: ServiceWorkerContainer

    @BeforeTest
    fun setup() {
        mockSdkContext = mock(MockMode.autofill)
        fakeLogger = FakeLogger()
        fakeServiceWorkerContainer = buildFakeServiceWorkerContainer()
        serviceWorkerManager =
            ServiceWorkerManager(mockSdkContext, fakeLogger, fakeServiceWorkerContainer)
    }

    private fun buildFakeServiceWorkerContainer(): ServiceWorkerContainer {
        val fakeRegistration = js("{}").unsafeCast<ServiceWorkerRegistration>()
        val container = js("{}")
        container.register = Promise.resolve(fakeRegistration)
        container.ready = Promise.resolve(Unit)
        return container.unsafeCast<ServiceWorkerContainer>()
    }

    @Test
    fun getServiceWorkerOptions_shouldReturnOptions_whenConfigHasServiceWorkerOptions() = runTest {
        val expectedOptions = ServiceWorkerOptions(
            applicationServerKey = "testKey",
            serviceWorkerPath = "/sw.js",
            serviceWorkerScope = "/scope"
        )
        val config = JsEngagementCloudSDKConfig(
            applicationCode = "testApp",
            serviceWorkerOptions = expectedOptions
        )
        everySuspend { mockSdkContext.getSdkConfig() } returns config

        val result = serviceWorkerManager.getServiceWorkerOptions()

        result shouldBe expectedOptions
        verifySuspend { mockSdkContext.getSdkConfig() }
    }

    @Test
    fun getServiceWorkerOptions_shouldReturnNull_whenConfigHasNoServiceWorkerOptions() = runTest {
        val config = JsEngagementCloudSDKConfig(applicationCode = "testApp")
        everySuspend { mockSdkContext.getSdkConfig() } returns config

        val result = serviceWorkerManager.getServiceWorkerOptions()

        result.shouldBeNull()
    }

    @Test
    fun getServiceWorkerOptions_shouldReturnNull_whenSdkConfigIsNull() = runTest {
        everySuspend { mockSdkContext.getSdkConfig() } returns null

        val result = serviceWorkerManager.getServiceWorkerOptions()

        result.shouldBeNull()
    }

    @Test
    fun register_shouldReturnFailure_whenSdkConfigIsNull() = runTest {
        everySuspend { mockSdkContext.getSdkConfig() } returns null

        val result = serviceWorkerManager.register()

        result.isFailure shouldBe true
    }

    @Test
    fun register_shouldReturnFailure_whenConfigHasNoServiceWorkerOptions() = runTest {
        val config = JsEngagementCloudSDKConfig(applicationCode = "testApp")
        everySuspend { mockSdkContext.getSdkConfig() } returns config

        val result = serviceWorkerManager.register()

        result.isFailure shouldBe true
    }

    @Test
    fun register_shouldReturnFailure_whenServiceWorkerContainerThrows() = runTest {
        val expectedOptions = ServiceWorkerOptions(
            applicationServerKey = "testKey",
            serviceWorkerPath = "/sw.js",
            serviceWorkerScope = "/scope"
        )
        val config = JsEngagementCloudSDKConfig(
            applicationCode = "testApp",
            serviceWorkerOptions = expectedOptions
        )
        everySuspend { mockSdkContext.getSdkConfig() } returns config

        val throwingContainer = buildThrowingServiceWorkerContainer()
        val manager = ServiceWorkerManager(mockSdkContext, fakeLogger, throwingContainer)

        val result = manager.register()

        result.isFailure shouldBe true
        (result.exceptionOrNull() is RuntimeException) shouldBe true
    }

    private fun buildThrowingServiceWorkerContainer(): ServiceWorkerContainer {
        val container = js("{}")
        container.register = { _: dynamic, _: dynamic ->
            throw RuntimeException("Registration failed")
        }
        return container.unsafeCast<ServiceWorkerContainer>()
    }
}
