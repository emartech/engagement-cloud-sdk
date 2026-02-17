package com.sap.ec.watchdog

import android.app.Activity
import com.sap.ec.watchdog.activity.TransitionSafeCurrentActivityWatchdog
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Before
import kotlin.system.measureTimeMillis
import kotlin.test.Test

class TransitionSafeActivityWatchdogTests {
    private lateinit var transitionSafeCurrentActivityWatchdog: TransitionSafeCurrentActivityWatchdog
    private lateinit var mockActivity: Activity

    @Before
    fun setup() {
        transitionSafeCurrentActivityWatchdog = TransitionSafeCurrentActivityWatchdog()
        mockActivity = mockk()
    }

    @Test
    fun waitForActivity_shouldReturn_activity_if_onResumed() = runTest {
        transitionSafeCurrentActivityWatchdog.register()

        transitionSafeCurrentActivityWatchdog.onActivityResumed(mockActivity)

        transitionSafeCurrentActivityWatchdog.waitForActivity() shouldBe mockActivity
    }

    @Test
    fun waitForActivity_shouldReturn_activity_if_onResumed_forAtLeast_500ms() = runTest {
        transitionSafeCurrentActivityWatchdog.register()
        transitionSafeCurrentActivityWatchdog.onActivityResumed(mockActivity)
        val duration = measureTimeMillis {
            transitionSafeCurrentActivityWatchdog.waitForActivity() shouldBe mockActivity
        }

        (duration >= 500) shouldBe true
    }

    @Test
    fun waitForActivity_shouldWaitForAnActivity_inResumed_beforeReturning() = runTest {
        transitionSafeCurrentActivityWatchdog.register()

        CoroutineScope(Dispatchers.Default).launch {
            delay(2000)
            transitionSafeCurrentActivityWatchdog.onActivityResumed(mockActivity)
        }

        val duration = measureTimeMillis {
            transitionSafeCurrentActivityWatchdog.waitForActivity() shouldBe mockActivity
        }

        (duration >= 2500) shouldBe true
    }

    @Test
    fun currentActivity_shouldReturn_null() = runTest {
        transitionSafeCurrentActivityWatchdog.register()

        transitionSafeCurrentActivityWatchdog.onActivityPaused(mockActivity)

        transitionSafeCurrentActivityWatchdog.currentActivity() shouldBe null
    }


    @Test
    fun currentActivity_shouldReturn_activity() = runTest {
        transitionSafeCurrentActivityWatchdog.register()

        transitionSafeCurrentActivityWatchdog.onActivityResumed(mockActivity)

        withContext(Dispatchers.Default) {
            delay(1000)
            transitionSafeCurrentActivityWatchdog.currentActivity() shouldBe mockActivity
        }
    }
}