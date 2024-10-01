package com.emarsys.watchdog

import android.app.Activity
import com.emarsys.watchdog.activity.TransitionSafeCurrentActivityWatchdog
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
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
    fun getCurrentActivity_shouldReturn_activity_if_onResumed() = runTest {
        transitionSafeCurrentActivityWatchdog.register()

        transitionSafeCurrentActivityWatchdog.onActivityResumed(mockActivity)

        transitionSafeCurrentActivityWatchdog.getCurrentActivity() shouldBe mockActivity
    }

    @Test
    fun getCurrentActivity_shouldReturn_activity_if_onResumed_forAtLeast_500ms() = runTest {
        transitionSafeCurrentActivityWatchdog.register()
        transitionSafeCurrentActivityWatchdog.onActivityResumed(mockActivity)
        val duration = measureTimeMillis {
            transitionSafeCurrentActivityWatchdog.getCurrentActivity() shouldBe mockActivity
        }

        (duration >= 500) shouldBe true
    }

    @Test
    fun getCurrentActivity_shouldWaitForAnActivity_inResumed_beforeReturning() = runTest {
        transitionSafeCurrentActivityWatchdog.register()

        CoroutineScope(Dispatchers.Default).launch {
            delay(2000)
            transitionSafeCurrentActivityWatchdog.onActivityResumed(mockActivity)
        }

        val duration = measureTimeMillis {
            transitionSafeCurrentActivityWatchdog.getCurrentActivity() shouldBe mockActivity
        }

        (duration >= 2500) shouldBe true
    }
}