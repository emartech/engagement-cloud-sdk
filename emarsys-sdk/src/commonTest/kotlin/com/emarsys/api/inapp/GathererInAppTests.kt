package com.emarsys.api.inapp


import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class GathererInAppTests {

    private lateinit var inAppContext: InAppApiContext

    private lateinit var gathererInApp: GathererInApp

    @BeforeTest
    fun setup() = runTest {
        inAppContext = InAppContext(mutableListOf())
        gathererInApp = GathererInApp(inAppContext)
    }

    @Test
    fun testIsPaused() = runTest {
        gathererInApp.isPaused shouldBe false

        inAppContext.inAppDnd = true

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
    }
}