package com.emarsys.watchdog.lifecycle

import androidx.lifecycle.Lifecycle
import com.emarsys.core.actions.LifecycleEvent
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test

class AndroidLifecycleWatchDogTests {

    private lateinit var androidLifecycleWatchDog: AndroidLifecycleWatchDog

    private lateinit var mockProcessLifecycleOwnerLifecycle: Lifecycle

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        mockProcessLifecycleOwnerLifecycle = mockk(relaxed = true)
        val processLifecycleOwnerScope = TestScope(StandardTestDispatcher())
        val lifecycleWatchDogScope = TestScope(StandardTestDispatcher())

        androidLifecycleWatchDog =
            AndroidLifecycleWatchDog(
                mockProcessLifecycleOwnerLifecycle,
                processLifecycleOwnerScope,
                lifecycleWatchDogScope
            )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testStart_shouldAddItselfAsLifecycleObserver() = runTest {
        androidLifecycleWatchDog.register()
        advanceUntilIdle()

        verify { mockProcessLifecycleOwnerLifecycle.addObserver(androidLifecycleWatchDog) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testOnStart_shouldEmitOnForeGroundLifecycleEvent() = runTest {
        androidLifecycleWatchDog.register()
        advanceUntilIdle()
        val events = mutableListOf<LifecycleEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            androidLifecycleWatchDog.lifecycleEvents.collect {
                events.add(it)
            }
        }

        androidLifecycleWatchDog.onStart(mockk(relaxed = true))
        advanceUntilIdle()

        events shouldBe listOf(LifecycleEvent.OnForeground)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testOnStop_shouldEmitOnBackGroundLifecycleEvent() = runTest {
        androidLifecycleWatchDog.register()
        advanceUntilIdle()
        val events = mutableListOf<LifecycleEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            androidLifecycleWatchDog.lifecycleEvents.collect {
                events.add(it)
            }
        }

        androidLifecycleWatchDog.onStop(mockk(relaxed = true))
        advanceUntilIdle()

        events shouldBe listOf(LifecycleEvent.OnBackground)
    }

}