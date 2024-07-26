package com.emarsys.integration

import com.emarsys.Emarsys
import com.emarsys.EmarsysConfig
import com.emarsys.di.DependencyContainerPrivateApi
import com.emarsys.di.DependencyInjection
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class ContactClientIntegrationTests {

    private lateinit var container: DependencyContainerPrivateApi

    @BeforeTest
    fun setup() = runTest {
        container = DependencyInjection.container as DependencyContainerPrivateApi

        Emarsys.initialize()
        Emarsys.enableTracking(EmarsysConfig("EMS11-C3FD3"))
    }

    @Test
    fun testLinkContact() = runTest {
        container.sessionContext.contactToken = null
        container.sessionContext.refreshToken = null

        container.contactClient.linkContact(2575, "test2@test.com")

        container.sessionContext.contactToken shouldNotBe null
        container.sessionContext.refreshToken shouldNotBe null
    }

    @Test
    fun testUnlinkContact() = runTest {
        container.contactClient.linkContact(2575, "test2@test.com")

        val contactToken = container.sessionContext.contactToken

        container.contactClient.unlinkContact()

        container.sessionContext.contactToken shouldNotBe contactToken
    }

}
