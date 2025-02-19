package com.emarsys.mobileengage.inapp

import androidx.test.core.app.ActivityScenario
import com.emarsys.FakeActivity
import com.emarsys.core.factory.Factory
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Before
import kotlin.test.Test


class InAppViewProviderTests {
    private lateinit var activityScenario: ActivityScenario<FakeActivity>
    private lateinit var provider: InAppViewProvider

    @Before
    fun setup() {
        val mockInAppJsBridgeProvider: Factory<String, InAppJsBridge> = mockk(relaxed = true)
        activityScenario =
            ActivityScenario.launch(FakeActivity::class.java)
        activityScenario.onActivity {
            provider =
                InAppViewProvider(
                    it.applicationContext,
                    mockInAppJsBridgeProvider,
                    Dispatchers.Main,
                    WebViewProvider(it.applicationContext, Dispatchers.Main)
                )
        }
    }

    @Test
    fun provide_shouldReturn_webInappViewInstance() = runTest {
        val view = provider.provide()

        (view is InAppView) shouldBe true
    }
}