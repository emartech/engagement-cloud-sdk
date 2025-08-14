package com.emarsys.api.config

import dev.mokkery.MockMode
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class GathererConfigTests {
    private companion object {
        const val APP_CODE = "testAppCode"
    }

    private lateinit var gathererConfig: GathererConfig
    private lateinit var configContext: ConfigContextApi

    @BeforeTest
    fun setUp() = runTest {
        configContext = ConfigContext(mutableListOf())
        gathererConfig = GathererConfig(configContext, sdkLogger = mock(MockMode.autofill))
    }

    @AfterTest
    fun tearDown() {
        configContext.calls.clear()
    }

    @Test
    fun testChangeApplicationCode_shouldStoreCall() = runTest {
        val expectedCall = ConfigCall.ChangeApplicationCode(APP_CODE)

        gathererConfig.changeApplicationCode(APP_CODE)

        configContext.calls.contains(expectedCall) shouldBe true
    }

    @Test
    fun testSetLanguage_shouldStoreCall() = runTest {
        val expectedCall = ConfigCall.SetLanguage("hu-HU")

        gathererConfig.setLanguage("hu-HU")

        configContext.calls.contains(expectedCall) shouldBe true
    }

    @Test
    fun testResetLanguage_shouldStoreCall() = runTest {
        val expectedCall = ConfigCall.ResetLanguage

        gathererConfig.resetLanguage()

        configContext.calls.contains(expectedCall) shouldBe true
    }
}