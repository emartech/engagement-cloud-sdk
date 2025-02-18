package com.emarsys.integration

import com.emarsys.Emarsys
import com.emarsys.EmarsysConfig
import com.emarsys.api.push.PushConstants
import com.emarsys.api.push.PushConstants.PUSH_TOKEN_STORAGE_KEY
import com.emarsys.di.DependencyContainerPrivateApi
import com.emarsys.di.DependencyInjection
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class PushClientIntegrationTests {

    private lateinit var container: DependencyContainerPrivateApi

    @BeforeTest
    fun setup() = runTest {
        container = DependencyInjection.container as DependencyContainerPrivateApi

        Emarsys.initialize()
        Emarsys.enableTracking(EmarsysConfig("EMS11-C3FD3"))
    }

    @Test
    fun testRegisterPushToken() = runTest {
        container.stringStorage.put(PUSH_TOKEN_STORAGE_KEY, null)

        Emarsys.push.registerPushToken("testPushToken")

        container.stringStorage.get(PUSH_TOKEN_STORAGE_KEY) shouldNotBe null
    }

    @Test
    fun testClearPushToken() = runTest {
        container.stringStorage.put(PUSH_TOKEN_STORAGE_KEY, "testPushToken")

        Emarsys.push.clearPushToken()

        container.stringStorage.get(PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY) shouldBe null
    }

}
