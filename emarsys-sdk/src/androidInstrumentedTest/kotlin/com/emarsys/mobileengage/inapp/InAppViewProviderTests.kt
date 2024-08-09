package com.emarsys.mobileengage.inapp

import androidx.test.core.app.ActivityScenario
import com.emarsys.FakeActivity
import com.emarsys.core.providers.Provider
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import kotlin.test.Test


class InAppViewProviderTests {
    private lateinit var activityScenario: ActivityScenario<FakeActivity>
    private lateinit var provider: InAppViewProvider

    @Before
    fun setup() {
        val mockJsBridge: InAppJsBridge = mockk(relaxed = true)
        val mockInAppJsBridgeProvider: Provider<InAppJsBridge> = mockk(relaxed = true)
        every { mockInAppJsBridgeProvider.provide() } returns mockJsBridge
        activityScenario =
            ActivityScenario.launch(FakeActivity::class.java)
        activityScenario.onActivity {
            provider = InAppViewProvider(it.applicationContext, mockInAppJsBridgeProvider)
        }
    }

    @Test
    fun provide_shouldReturn_webInappViewInstance() {
        activityScenario.onActivity {
            val view = provider.provide()

            (view is InAppView) shouldBe true
        }
    }
}