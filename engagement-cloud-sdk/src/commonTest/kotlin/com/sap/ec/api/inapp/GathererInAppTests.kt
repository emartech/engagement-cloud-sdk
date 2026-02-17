package com.sap.ec.api.inapp

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class GathererInAppTests {
    private lateinit var gathererInApp: GathererInApp
    private lateinit var inAppContext: InAppContextApi
    private lateinit var inAppConfig: InAppConfig

    @BeforeTest
    fun setup() = runTest {
        inAppContext = InAppContext(mutableListOf())
        inAppConfig = InAppConfig()

        gathererInApp = GathererInApp(inAppContext, inAppConfig)
    }

    @Test
    fun testIsPaused() = runTest {
        gathererInApp.isPaused shouldBe false

        inAppConfig.inAppDnd = true

        gathererInApp.isPaused shouldBe true
    }

    @Test
    fun testPause_shouldAddCallToContext() = runTest {
        val testCall = InAppCall.Pause()

        gathererInApp.pause()
        inAppContext.calls.contains(testCall) shouldBe true
    }

    @Test
    fun testResume_shouldAddCallToContext() = runTest {
        val testCall = InAppCall.Resume()

        gathererInApp.resume()

        inAppContext.calls.contains(testCall) shouldBe true
        inAppContext.calls.size shouldBe 1
    }
}