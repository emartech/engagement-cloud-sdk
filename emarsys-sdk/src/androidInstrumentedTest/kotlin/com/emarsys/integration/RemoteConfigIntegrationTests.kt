package com.emarsys.integration

import com.emarsys.Emarsys
import com.emarsys.EmarsysConfig
import com.emarsys.context.SdkContextApi
import com.emarsys.di.SdkKoinIsolationContext.koin
import com.emarsys.remoteConfig.RemoteConfigHandlerApi
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.koin.core.Koin
import org.koin.core.component.get
import org.koin.test.KoinTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class RemoteConfigIntegrationTests: KoinTest {
    override fun getKoin(): Koin = koin

    private companion object {
        private const val INTEGRATION_TEST_APP_CODE = "integrationTest"
    }

    @BeforeTest
    fun setUp() = runTest {
        Emarsys.initialize()
    }

    @Test
    fun testRemoteConfig_with_integrationTest_app_code() = runTest {
        val sdkContext: SdkContextApi = get()
        sdkContext.config = EmarsysConfig(INTEGRATION_TEST_APP_CODE)

        val remoteConfigHandler: RemoteConfigHandlerApi = get()
        remoteConfigHandler.handleAppCodeBased()

        val defaultUrls = sdkContext.defaultUrls
        val clientServiceBaseUrl = defaultUrls.clientServiceBaseUrl
        val eventServiceBaseUrl = defaultUrls.eventServiceBaseUrl
        clientServiceBaseUrl shouldBe "https://integration.me-client.eservice.emarsys.net"
        eventServiceBaseUrl shouldBe "https://integration.mobile-events.eservice.emarsys.net"
    }
}