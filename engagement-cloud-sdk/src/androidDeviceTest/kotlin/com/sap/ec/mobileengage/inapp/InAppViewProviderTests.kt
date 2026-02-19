package com.sap.ec.mobileengage.inapp

import androidx.test.core.app.ActivityScenario
import com.sap.ec.FakeActivity
import com.sap.ec.core.factory.Factory
import com.sap.ec.core.providers.TimestampProvider
import com.sap.ec.mobileengage.inapp.jsbridge.ContentReplacerApi
import com.sap.ec.mobileengage.inapp.jsbridge.InAppJsBridgeData
import com.sap.ec.mobileengage.inapp.provider.InAppViewProvider
import com.sap.ec.mobileengage.inapp.provider.WebViewProvider
import com.sap.ec.mobileengage.inapp.view.InAppView
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Before
import kotlin.test.Test


class InAppViewProviderTests {
    private lateinit var activityScenario: ActivityScenario<FakeActivity>
    private lateinit var provider: InAppViewProvider
    private lateinit var mockContentReplacer: ContentReplacerApi

    @Before
    fun setup() {
        val mockInAppJsBridgeProvider: Factory<InAppJsBridgeData, InAppJsBridge> =
            mockk(relaxed = true)
        mockContentReplacer = mockk()
        activityScenario =
            ActivityScenario.launch(FakeActivity::class.java)
        activityScenario.onActivity {
            provider =
                InAppViewProvider(
                    it.applicationContext,
                    mockInAppJsBridgeProvider,
                    Dispatchers.Main,
                    WebViewProvider(it.applicationContext, Dispatchers.Main),
                    TimestampProvider(),
                    contentReplacer = mockContentReplacer
                )
        }
    }

    @Test
    fun provide_shouldReturn_webInAppViewInstance() = runTest {
        val view = provider.provide()

        (view is InAppView) shouldBe true
    }
}