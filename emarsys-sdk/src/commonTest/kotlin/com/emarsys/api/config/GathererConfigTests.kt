package com.emarsys.api.config

import dev.mokkery.MockMode
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class GathererConfigTests {
    private companion object {
        const val APP_CODE = "testAppCode"
        const val MERCHANT_ID = "testMerchantId"
    }

    private lateinit var configContext: ConfigContext
    private lateinit var gathererConfig: GathererConfig

    @BeforeTest
    fun setUp() = runTest {
        configContext = ConfigContext(mutableListOf())
        gathererConfig = GathererConfig(configContext, sdkLogger = mock(MockMode.autofill))
    }

    @Test
    fun testChangeApplicationCode_shouldStoreCall() = runTest {
        val expectedCall = ConfigCall.ChangeApplicationCode(APP_CODE)

        gathererConfig.changeApplicationCode(APP_CODE)

        configContext.calls.contains(expectedCall) shouldBe true
    }

    @Test
    fun testChangeMerchantId_shouldStoreCall() = runTest {
        val expectedCall = ConfigCall.ChangeMerchantId(MERCHANT_ID)

        gathererConfig.changeMerchantId(MERCHANT_ID)

        configContext.calls.contains(expectedCall) shouldBe true
    }
}