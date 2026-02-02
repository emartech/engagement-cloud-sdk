package com.emarsys.api.deeplink

import com.emarsys.context.SdkContextApi
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.event.SdkEvent
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture.Companion.slot
import dev.mokkery.matcher.capture.SlotCapture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import io.ktor.http.Url
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DeepLinkInternalTests {
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi
    private lateinit var eventSlot: SlotCapture<SdkEvent>
    private lateinit var testDispatcher: TestDispatcher
    private lateinit var deepLinkInternal: DeepLinkInternal

    @BeforeTest
    fun setUp() {
        testDispatcher = StandardTestDispatcher()
        mockSdkContext = mock(MockMode.autofill)
        every { mockSdkContext.sdkDispatcher } returns testDispatcher

        eventSlot = slot()
        mockSdkEventDistributor = mock(MockMode.autofill)
        everySuspend { mockSdkEventDistributor.registerEvent(capture(eventSlot)) } returns mock(
            MockMode.autofill
        )
        deepLinkInternal = DeepLinkInternal(mockSdkContext, mockSdkEventDistributor)
    }

    @Test
    fun testTrack_should_emit_trackEvent_into_sdkEventFlow_and_return_true_if_url_contains_ems_dl_param() =
        runTest {
            val url = Url("https://example.com?ems_dl=123")

            val result = deepLinkInternal.track(url)

            val emittedEvent = eventSlot.get()

            (emittedEvent is SdkEvent.Internal.Sdk.TrackDeepLink) shouldBe true
            (emittedEvent as SdkEvent.Internal.Sdk.TrackDeepLink).trackingId shouldBe "123"
            result.getOrNull() shouldBe true
        }

    @Test
    fun testTrack_should_return_false_if_url_doesnt_contain_ems_dl_param() =
        runTest {
            val url = Url("https://example.com?any_param=any")

            val result = deepLinkInternal.track(url)

            result.getOrNull() shouldBe false
            verifySuspend(VerifyMode.exactly(0)) { mockSdkEventDistributor.registerEvent(any()) }
        }

}
