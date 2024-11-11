package com.emarsys.core.badge

import com.emarsys.api.push.PushConstants.WEB_PUSH_ON_BADGE_COUNT_UPDATE_RECEIVED
import com.emarsys.mobileengage.action.models.BadgeCount
import com.emarsys.mobileengage.action.models.BadgeCountMethod
import com.emarsys.mobileengage.events.SdkEvent
import com.emarsys.mobileengage.events.SdkEventSource
import com.emarsys.util.JsonUtil
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.encodeToString
import web.broadcast.BroadcastChannel
import kotlin.test.BeforeTest
import kotlin.test.Test

class WebBadgeCountHandlerTest {

    private lateinit var sdkEventFlow: MutableSharedFlow<SdkEvent>
    private lateinit var onBadgeCountUpdateReceivedBroadcastChannel: BroadcastChannel
    private lateinit var webBadgeCountHandler: WebBadgeCountHandler

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeTest
    fun setup() = runTest {
        Dispatchers.setMain(StandardTestDispatcher())

        sdkEventFlow = MutableSharedFlow()
        onBadgeCountUpdateReceivedBroadcastChannel =
            BroadcastChannel(WEB_PUSH_ON_BADGE_COUNT_UPDATE_RECEIVED)
        webBadgeCountHandler =
            WebBadgeCountHandler(
                onBadgeCountUpdateReceivedBroadcastChannel,
                sdkEventFlow,
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun handleBadgeCount_shouldEmitSdkEvent_whenBadgeCountReceived() = runTest {
        val testBadgeCount = BadgeCount(BadgeCountMethod.SET, 1)
        val testBadgeCountString = JsonUtil.json.encodeToString(testBadgeCount)
        val events = mutableListOf<SdkEvent>()

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            sdkEventFlow.collect {
                events.add(it)
            }
        }

        webBadgeCountHandler.handleBadgeCount(testBadgeCountString)

        events.size shouldBe 1
        events[0].let {
            it.source shouldBe SdkEventSource.BadgeCount
            it.name shouldBe testBadgeCount.method.name
            it.payload?.get("badgeCount") shouldBe testBadgeCount.value
        }
    }


}