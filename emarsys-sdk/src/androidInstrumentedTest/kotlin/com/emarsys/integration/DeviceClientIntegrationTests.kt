package com.emarsys.integration

import com.emarsys.Emarsys
import com.emarsys.EmarsysConfig
import com.emarsys.di.DependencyContainerPrivateApi
import com.emarsys.di.DependencyInjection
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class DeviceClientIntegrationTests {

    @Test
    fun testRegisterDeviceInfo() = runTest {
        val container = DependencyInjection.container as DependencyContainerPrivateApi

        Emarsys.initialize()
        Emarsys.enableTracking(EmarsysConfig("EMS11-C3FD3"))

        container.sessionContext.contactToken = null
        container.sessionContext.refreshToken = null
        container.sessionContext.clientState = null

        container.deviceClient.registerDeviceInfo()

        container.sessionContext.contactToken shouldNotBe null
        container.sessionContext.refreshToken shouldNotBe null
        container.sessionContext.clientState shouldNotBe null
    }

}