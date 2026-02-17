package com.sap.ec.watchdog.lifecycle

import com.sap.ec.core.lifecycle.LifecycleEvent
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import web.dom.document
import web.events.Event
import web.events.EventType
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WebConnectionWatchDogTests {

    private lateinit var webLifeCycleWatchDog: WebLifeCycleWatchDog

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }


    @Test
    fun testStart_shouldAddVisibilityChangeEventListener() = runTest {
        webLifeCycleWatchDog = WebLifeCycleWatchDog(document, TestScope())
        webLifeCycleWatchDog.register()
        val events = mutableListOf<LifecycleEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
           webLifeCycleWatchDog.lifecycleEvents.collect {
                events.add(it)
           }
        }

        document.dispatchEvent(
            Event(
                EventType("visibilitychange")
            )
        )
        advanceUntilIdle()

        events shouldBe listOf(LifecycleEvent.OnForeground)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testStart_shouldAddPageHideEventListener() = runTest {
        webLifeCycleWatchDog = WebLifeCycleWatchDog(document, TestScope())
        webLifeCycleWatchDog.register()
        val events = mutableListOf<LifecycleEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            webLifeCycleWatchDog.lifecycleEvents.collect {
                events.add(it)
            }
        }

        document.dispatchEvent(
            Event(
                EventType("pagehide")
            )
        )
        advanceUntilIdle()

        events shouldBe listOf(LifecycleEvent.OnBackground)
    }
}