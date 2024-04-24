package com.emarsys.api.inapp

import com.emarsys.api.AppEvent
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class GathererInAppTests {
    private companion object {
        val testEvents = MutableSharedFlow<AppEvent>()
    }

    private lateinit var inAppContext: InAppApiContext

    private lateinit var gathererInApp: GathererInApp

    @BeforeTest
    fun setup() = runTest {
        inAppContext = InAppContext(mutableListOf())
        gathererInApp = GathererInApp(inAppContext, testEvents)
    }

    @Test
    fun testIsPaused() = runTest {
        gathererInApp.isPaused shouldBe false

        inAppContext.inAppDnd = true

        gathererInApp.isPaused shouldBe true
    }

    @Test
    fun testEvent() = runTest {
        gathererInApp.events shouldBe testEvents
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