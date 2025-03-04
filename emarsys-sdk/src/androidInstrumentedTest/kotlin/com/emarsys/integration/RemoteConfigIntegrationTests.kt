package com.emarsys.integration

import com.emarsys.Emarsys
import com.emarsys.EmarsysConfig
import com.emarsys.di.DependencyContainer
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class RemoteConfigIntegrationTests {

    private companion object {
        private const val INTEGRATION_TEST_APP_CODE = "integrationTest"
    }

    @BeforeTest
    fun setUp() = runTest {
        Emarsys.initialize()
    }

    @Test
    fun testRemoteConfig_with_integrationTest_app_code() = runTest {
        val dependencyContainer = DependencyContainer()
        dependencyContainer.sdkContext.config = EmarsysConfig(INTEGRATION_TEST_APP_CODE)
        dependencyContainer.remoteConfigHandler.handleAppCodeBased()
        val defaultUrls = dependencyContainer.sdkContext.defaultUrls
        val clientServiceBaseUrl = defaultUrls.clientServiceBaseUrl
        val eventServiceBaseUrl = defaultUrls.eventServiceBaseUrl
        clientServiceBaseUrl shouldBe "https://integration.me-client.eservice.emarsys.net"
        eventServiceBaseUrl shouldBe "https://integration.mobile-events.eservice.emarsys.net"
    }
}