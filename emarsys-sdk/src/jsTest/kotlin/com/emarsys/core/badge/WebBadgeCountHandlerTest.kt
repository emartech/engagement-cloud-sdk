package com.emarsys.core.badge

import com.emarsys.api.push.PushConstants.WEB_PUSH_ON_BADGE_COUNT_UPDATE_RECEIVED
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.mobileengage.action.models.BadgeCount
import com.emarsys.mobileengage.action.models.BadgeCountMethod
import com.emarsys.networking.clients.event.model.SdkEvent
import com.emarsys.util.JsonUtil
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import web.broadcast.BroadcastChannel
import kotlin.test.BeforeTest
import kotlin.test.Test

class WebBadgeCountHandlerTest {

    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi
    private lateinit var onBadgeCountUpdateReceivedBroadcastChannel: BroadcastChannel
    private lateinit var webBadgeCountHandler: WebBadgeCountHandler

    @OptIn(ExperimentalCoroutinesApi::class)
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
        val testBadgeCount = BadgeCount(BadgeCountMethod.SET, 1)
        val testBadgeCountString = JsonUtil.json.encodeToString(testBadgeCount)
        everySuspend { mockSdkEventDistributor.registerAndStoreEvent(any()) } returns Unit

        webBadgeCountHandler.handleBadgeCount(testBadgeCountString)

        verifySuspend {
            mockSdkEventDistributor.registerAndStoreEvent(
                SdkEvent.External.Api.BadgeCount(
                    id = any(),
                    name = testBadgeCount.method.name,
                    attributes = buildJsonObject {
                        put(
                            "badgeCount",
                            testBadgeCount.value
                        )
                    }
                ))
        }
    }


}