package com.emarsys.integration

import com.emarsys.Emarsys
import com.emarsys.EmarsysConfig
import com.emarsys.di.DependencyContainerPrivateApi
import com.emarsys.di.DependencyInjection
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Ignore
import kotlin.test.Test

class DeviceClientIntegrationTests {

    @Ignore //TODO: V4 client endpoint does not return tokens at the moment(31.10.2024); test should pass after BE is fixed
    @Test
    fun testRegisterDeviceInfo_whenAnonymousContactIsTurnedOn() = runTest {
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

    @Ignore //TODO: this is a real test case when the backend get rid off their inner flipper "SET_ANON_CONTACT_ON_CLIENT_REGISTRATION"
    @Test
    fun testRegisterDeviceInfo_whenAnonymousContactIsTurnedOff() = runTest {
        val container = DependencyInjection.container as DependencyContainerPrivateApi

        Emarsys.initialize()
        Emarsys.enableTracking(EmarsysConfig("EMSD1-A342A"))

        container.sessionContext.contactToken = null
        container.sessionContext.refreshToken = null
        container.sessionContext.clientState = null

        container.deviceClient.registerDeviceInfo()

        container.sessionContext.contactToken shouldBe null
        container.sessionContext.refreshToken shouldBe null
        container.sessionContext.clientState shouldBe null
    }

}
