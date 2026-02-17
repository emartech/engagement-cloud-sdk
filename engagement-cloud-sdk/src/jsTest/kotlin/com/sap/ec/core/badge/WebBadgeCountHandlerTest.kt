package com.sap.ec.core.badge

import com.sap.ec.api.push.PushConstants.WEB_PUSH_ON_BADGE_COUNT_UPDATE_RECEIVED
import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.event.SdkEvent
import com.sap.ec.mobileengage.action.models.BadgeCount
import com.sap.ec.mobileengage.action.models.BadgeCountMethod
import com.sap.ec.util.JsonUtil
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture.Companion.slot
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.isPresent
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import web.broadcast.BroadcastChannel
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class WebBadgeCountHandlerTest {

    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi
    private lateinit var onBadgeCountUpdateReceivedBroadcastChannel: BroadcastChannel
    private lateinit var webBadgeCountHandler: WebBadgeCountHandler

    @BeforeTest
    fun setup() = runTest {
        Dispatchers.setMain(StandardTestDispatcher())

        mockSdkEventDistributor = mock()
        onBadgeCountUpdateReceivedBroadcastChannel =
            BroadcastChannel(WEB_PUSH_ON_BADGE_COUNT_UPDATE_RECEIVED)
        webBadgeCountHandler =
            WebBadgeCountHandler(
                onBadgeCountUpdateReceivedBroadcastChannel,
                mockSdkEventDistributor,
                TestScope(),
                sdkLogger = mock {
                    everySuspend {
                        error(
                            any<String>(),
                            any<Throwable>()
                        )
                    } returns Unit
                }
            )
    }

    @Test
    fun register_shouldRegisterOnMessageListener_onOnBadgeCountUpdateReceivedBroadcastChannel() =
        runTest {
            webBadgeCountHandler.register()

            onBadgeCountUpdateReceivedBroadcastChannel.onmessage shouldNotBe null
        }

    @Test
    fun handleBadgeCount_shouldEmitSdkEvent_whenBadgeCountReceived() = runTest {
        val eventSlot = slot<SdkEvent.External.Api.BadgeCountEvent>()
        val testBadgeCount = BadgeCount(BadgeCountMethod.SET, 1)
        val testBadgeCountString = JsonUtil.json.encodeToString(testBadgeCount)
        everySuspend { mockSdkEventDistributor.registerEvent(any()) } returns mock(MockMode.autofill)

        everySuspend {
            mockSdkEventDistributor.registerEvent(
                capture(eventSlot)
            )
        } returns mock(MockMode.autofill)

        webBadgeCountHandler.handleBadgeCount(testBadgeCountString)

        eventSlot.isPresent shouldBe true
    }
}