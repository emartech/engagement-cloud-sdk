package com.sap.ec.core.badge

import com.sap.ec.api.push.PushConstants.WEB_PUSH_ON_BADGE_COUNT_UPDATE_RECEIVED
import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.log.Logger
import com.sap.ec.api.event.model.BadgeCountEvent
import com.sap.ec.mobileengage.action.models.BadgeCount
import com.sap.ec.mobileengage.action.models.BadgeCountMethod
import com.sap.ec.util.JsonUtil
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture.Companion.slot
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.matcher.capture.isPresent
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
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
        val eventSlot = slot<BadgeCountEvent>()
        val testBadgeCount = BadgeCount(BadgeCountMethod.SET, 1)
        val testBadgeCountString = JsonUtil.json.encodeToString(testBadgeCount)
        val completableDeferred = CompletableDeferred<Unit>()
        everySuspend { mockSdkEventDistributor.registerPublicEvent(capture(eventSlot)) } calls {
            completableDeferred.complete(Unit)
        }

        webBadgeCountHandler.handleBadgeCount(testBadgeCountString)

        completableDeferred.await()
        with(eventSlot.get()) {
            method shouldBe BadgeCountMethod.SET.name
            badgeCount shouldBe 1
        }
        eventSlot.isPresent shouldBe true
    }

    @Test
    fun handleBadgeCount_shouldPropagateCancellationException_whenCoroutineIsCancelled() =
        runTest {
            val testJob = Job()
            val testScope = CoroutineScope(StandardTestDispatcher(testScheduler) + testJob)
            val mockLogger = mock<Logger> {
                everySuspend { error(any<String>(), any<Throwable>()) } returns Unit
            }
            val handler = WebBadgeCountHandler(
                onBadgeCountUpdateReceivedBroadcastChannel,
                mockSdkEventDistributor,
                testScope,
                sdkLogger = mockLogger
            )
            val testBadgeCount = BadgeCount(BadgeCountMethod.SET, 1)
            val testBadgeCountString = JsonUtil.json.encodeToString(testBadgeCount)

            everySuspend { mockSdkEventDistributor.registerPublicEvent(any()) } calls {
                testJob.cancel()
                throw CancellationException("Job was cancelled")
            }

            testScope.launch {
                handler.handleBadgeCount(testBadgeCountString)
            }
            advanceUntilIdle()

            verifySuspend(VerifyMode.exactly(0)) {
                mockLogger.error("WebBadgeCountHandler - handleBadgeCount", any<Throwable>())
            }
        }
}