package com.emarsys.api.deeplink

import com.emarsys.context.DefaultUrls
import com.emarsys.context.SdkContext
import com.emarsys.context.SdkContextApi
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.log.LogLevel
import com.emarsys.networking.clients.event.model.SdkEvent
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.capture.Capture.Companion.slot
import dev.mokkery.matcher.capture.SlotCapture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import io.ktor.http.Url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DeepLinkInternalTests {

    private lateinit var sdkContext: SdkContextApi
    private lateinit var deepLinkInternal: DeepLinkInternal
    private lateinit var sdkEventDistributor: SdkEventDistributorApi
    private lateinit var eventSlot: SlotCapture<SdkEvent>

    @BeforeTest
    fun setUp() {
        val mainDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(mainDispatcher)
        sdkContext = SdkContext(
            StandardTestDispatcher(),
            mainDispatcher,
            DefaultUrls("", "", "", "", "", "", ""),
            LogLevel.Error,
            mutableSetOf(),
            logBreadcrumbsQueueSize = 10
        )

        eventSlot = slot()
        sdkEventDistributor = mock(MockMode.autofill)
        everySuspend { sdkEventDistributor.registerEvent(capture(eventSlot)) } returns mock(MockMode.autofill)
        deepLinkInternal = DeepLinkInternal(sdkContext, sdkEventDistributor)
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testTrackDeepLink_should_emit_trackDeepLinkEvent_into_sdkEventFlow_if_url_contains_ems_dl_param() =
        runTest {
            val url = Url("https://example.com?ems_dl=123")

            deepLinkInternal.trackDeepLink(url)

            val emittedEvent = eventSlot.get()

            (emittedEvent is SdkEvent.Internal.Sdk.TrackDeepLink) shouldBe true
            emittedEvent.attributes?.get("trackingId")?.jsonPrimitive?.content shouldBe "123"
        }
}
