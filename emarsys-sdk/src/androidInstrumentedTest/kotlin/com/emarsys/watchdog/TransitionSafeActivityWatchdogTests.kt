package com.emarsys.watchdog

import android.app.Activity
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import com.emarsys.FakeActivity
import com.emarsys.watchdog.activity.TransitionSafeCurrentActivityWatchdog
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Before
import kotlin.system.measureTimeMillis
import kotlin.test.Test

class TransitionSafeActivityWatchdogTests {
    private lateinit var activityScenario: ActivityScenario<FakeActivity>
    private lateinit var activityScenario2: ActivityScenario<FakeActivity>
    private lateinit var transitionSafeCurrentActivityWatchdog: TransitionSafeCurrentActivityWatchdog

    @Before
    fun setup() {
        transitionSafeCurrentActivityWatchdog = TransitionSafeCurrentActivityWatchdog()
        activityScenario =
            ActivityScenario.launch(FakeActivity::class.java).also {
                it.moveToState(Lifecycle.State.CREATED)
            }
    }

    @Test
    fun getCurrentActivity_shouldReturn_activity_if_onResumed() = runTest {
        transitionSafeCurrentActivityWatchdog.register()
        var activity: Activity? = null
        activityScenario.onActivity {
            activity = it
        }

        activityScenario.moveToState(Lifecycle.State.RESUMED)

        transitionSafeCurrentActivityWatchdog.getCurrentActivity() shouldBe activity
    }

    @Test
    fun getCurrentActivity_shouldReturn_activity_if_onResumed_forAtLeast_500ms() = runTest {
        transitionSafeCurrentActivityWatchdog.register()

        var activity2: Activity? = null
        val duration = measureTimeMillis {
            activityScenario2 = ActivityScenario.launch(FakeActivity::class.java).also {
                it.onActivity { activity ->
                    activity2 = activity
                }
            }

            transitionSafeCurrentActivityWatchdog.getCurrentActivity() shouldBe activity2
        }

        (duration >= 500) shouldBe true
    }

    @Test
    fun getCurrentActivity_shouldWaitForAnActivity_inResumed_beforeReturning() = runTest {
        transitionSafeCurrentActivityWatchdog.register()
        var activity: Activity? = null
        activityScenario.onActivity {
            activity = it
        }

        CoroutineScope(Dispatchers.Default).launch {
            delay(2000)
            activityScenario.moveToState(Lifecycle.State.RESUMED)
        }

        val duration = measureTimeMillis {
            transitionSafeCurrentActivityWatchdog.getCurrentActivity() shouldBe activity
        }

        (duration >= 2500) shouldBe true
    }
}