package com.emarsys.api.inapp

import com.emarsys.api.AppEvent
import com.emarsys.context.DefaultUrls
import com.emarsys.context.SdkContext
import com.emarsys.context.SdkContextApi
import com.emarsys.core.log.LogLevel
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class GathererInAppTests {
    private companion object {
        val testEvents = MutableSharedFlow<AppEvent>()
    }

    private lateinit var inAppContext: InAppContext

    private lateinit var sdkContext: SdkContextApi

    private lateinit var gathererInApp: GathererInApp

    @BeforeTest
    fun setup() = runTest {
        sdkContext = SdkContext(
            StandardTestDispatcher(),
            DefaultUrls("", "", "", "", "", "", ""),
            LogLevel.Error,
            mutableSetOf()
        )

        inAppContext = InAppContext(mutableListOf())
        gathererInApp = GathererInApp(inAppContext, sdkContext, testEvents)
    }

    @Test
    fun testIsPaused() = runTest {
        gathererInApp.isPaused shouldBe false

        sdkContext.inAppDnd = true

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