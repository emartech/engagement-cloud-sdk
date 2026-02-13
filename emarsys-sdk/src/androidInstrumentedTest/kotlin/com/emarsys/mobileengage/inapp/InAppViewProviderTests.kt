package com.emarsys.mobileengage.inapp

import androidx.test.core.app.ActivityScenario
import com.emarsys.FakeActivity
import com.emarsys.core.factory.Factory
import com.emarsys.core.providers.TimestampProvider
import com.emarsys.mobileengage.inapp.jsbridge.InAppJsBridgeData
import com.emarsys.mobileengage.inapp.provider.InAppViewProvider
import com.emarsys.mobileengage.inapp.provider.WebViewProvider
import com.emarsys.mobileengage.inapp.view.InAppView
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
        val mockInAppJsBridgeProvider: Factory<InAppJsBridgeData, InAppJsBridge> =
            mockk(relaxed = true)
        activityScenario =
            ActivityScenario.launch(FakeActivity::class.java)
        activityScenario.onActivity {
            provider =
                InAppViewProvider(
                    it.applicationContext,
                    mockInAppJsBridgeProvider,
                    Dispatchers.Main,
                    WebViewProvider(it.applicationContext, Dispatchers.Main),
                    TimestampProvider()
                )
        }
    }

    @Test
    fun provide_shouldReturn_webInAppViewInstance() = runTest {
        val view = provider.provide()

        (view is InAppView) shouldBe true
    }
}