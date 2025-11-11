@file:OptIn(ExperimentalTime::class)

package com.emarsys.api.events

import EmarsysSdkEventListener
import com.emarsys.api.events.SdkApiEventTypes.SDK_APP_EVENT
import com.emarsys.api.events.SdkApiEventTypes.SDK_BADGE_COUNT_EVENT
import com.emarsys.core.log.Logger
import com.emarsys.core.providers.UuidProviderApi
import com.emarsys.event.SdkEvent
import com.emarsys.util.JsonUtil
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import js.json.stringify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class)
class EventEmitterTests {
    private companion object {
        const val ID = "testId"
        const val ID2 = "testId2"
        const val ID3 = "testId3"
        const val NAME = "testName"
        const val NAME2 = "testName2"
        const val UUID = "testUuid"
        val testTimestamp = Clock.System.now()
        val testAttributes = buildJsonObject { put("key", "value") }
        val testAttributes2 = buildJsonObject { put("key2", "value2") }
        val testAppEvent1 =
            SdkEvent.External.Api.AppEvent(ID, NAME, testAttributes, testTimestamp)
        val testAppEvent2 =
            SdkEvent.External.Api.AppEvent(ID2, NAME2, testAttributes2, testTimestamp)
        val testBadgeCountEvent =
            SdkEvent.External.Api.BadgeCountEvent(
                ID3,
                testTimestamp,
                badgeCount = 10,
                method = "add"
            )
    }

    private lateinit var sdkOutboundEventFLow: MutableSharedFlow<SdkEvent.External.Api>
    private lateinit var listeners: MutableMap<String, MutableList<EmarsysSdkEventListener>>
    private lateinit var onceListeners: MutableMap<String, EmarsysSdkEventListener>
    private lateinit var mockUuidProvider: UuidProviderApi
    private lateinit var json: Json
    private lateinit var mockLogger: Logger

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        sdkOutboundEventFLow = MutableSharedFlow()
        listeners = mutableMapOf()
        onceListeners = mutableMapOf()
        json = JsonUtil.json
        mockLogger = mock(MockMode.autofill)
        mockUuidProvider = mock()
        every { mockUuidProvider.provide() } returns UUID
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createEmitter(
        applicationScope: CoroutineScope,
    ): EventEmitter {
        return EventEmitter(
            sdkOutboundEventFLow,
            applicationScope,
            listeners,
            onceListeners,
            mockUuidProvider,
            json,
            mockLogger
        )
    }

    @Test
    fun on_shouldRegisterTheListener_sendDebugLog_startCollection_andCallTheListener_whenAnEventIsEmitted() =
        runTest {
            val emitter = createEmitter(backgroundScope)
            var handledEvent: SdkApiEvent? = null
            val testListener: EmarsysSdkEventListener = { handledEvent = it }

            emitter.on(SDK_APP_EVENT, testListener)

            sdkOutboundEventFLow.emit(testAppEvent1)

            advanceUntilIdle()

            handledEvent shouldNotBe null

            val result = handledEvent as SdkApiAppEvent
            result.id shouldBe ID
            result.name shouldBe NAME
            stringify(result.attributes) shouldBe testAttributes.toString()
            handledEvent?.type shouldBe SDK_APP_EVENT

            verifySuspend { mockLogger.debug("Registering listener for event type: $SDK_APP_EVENT") }
        }

    @Test
    fun emitter_shouldInvokeEveryListener_forTheEmittedEventType_andNotInvokeOtherListenerTypes() =
        runTest {
            val emitter = createEmitter(backgroundScope)
            val handledAppEvents1: MutableList<SdkApiEvent> = mutableListOf()
            val handledAppEvents2: MutableList<SdkApiEvent> = mutableListOf()
            var handledBadgeCountEvent: SdkApiEvent? = null
            val testAppEventListener1: EmarsysSdkEventListener = { handledAppEvents1.add(it) }
            val testBadgeCountListener: EmarsysSdkEventListener = { handledBadgeCountEvent = it }
            val testAppEventListener2: EmarsysSdkEventListener = { handledAppEvents2.add(it) }

            emitter.on(SDK_APP_EVENT, testAppEventListener1)
            emitter.on(SDK_BADGE_COUNT_EVENT, testBadgeCountListener)
            emitter.on(SDK_APP_EVENT, testAppEventListener2)

            sdkOutboundEventFLow.emit(testAppEvent1)
            sdkOutboundEventFLow.emit(testAppEvent2)

            advanceUntilIdle()

            handledAppEvents1.size shouldBe 2
            handledAppEvents2.size shouldBe 2
            handledBadgeCountEvent shouldBe null

            listOf(handledAppEvents1, handledAppEvents2).forEach { eventList ->
                val event1 = eventList.find { it.id === ID } as SdkApiAppEvent
                event1.type shouldBe SDK_APP_EVENT
                event1.name shouldBe NAME
                JSON.stringify(event1.attributes) shouldBe testAttributes.toString()

                val event2 = eventList.find { it.id === ID2 } as SdkApiAppEvent
                event2.type shouldBe SDK_APP_EVENT
                event2.name shouldBe NAME2
                JSON.stringify(event2.attributes) shouldBe testAttributes2.toString()
            }
        }

    @Test
    fun emitter_shouldStopCollection_ifNoEventListenersAreLeft_afterCalling_onceListener() =
        runTest {
            val emitter = createEmitter(backgroundScope)
            val handledAppEvents: MutableList<SdkApiEvent> = mutableListOf()
            val testAppEventListener: EmarsysSdkEventListener = { handledAppEvents.add(it) }

            emitter.once(SDK_APP_EVENT, testAppEventListener)

            sdkOutboundEventFLow.emit(testAppEvent1)

            advanceUntilIdle()

            handledAppEvents.size shouldBe 1

            val event1 = handledAppEvents.find { it.id === ID } as SdkApiAppEvent
            event1.type shouldBe SDK_APP_EVENT
            event1.name shouldBe NAME
            JSON.stringify(event1.attributes) shouldBe testAttributes.toString()

            handledAppEvents.find { it.id === ID2 } shouldBe null

            sdkOutboundEventFLow.emit(testAppEvent2)

            advanceUntilIdle()

            handledAppEvents.size shouldBe 1
            handledAppEvents.find { it.id === ID2 } shouldBe null
        }

    @Test
    fun emitter_shouldKeepCollecting_ifErrorOccurs_onListenerInvocation_andLogTheError() =
        runTest {
            val emitter = createEmitter(backgroundScope)
            val testError: Throwable = Exception("Failure")

            val handledAppEvents: MutableList<SdkApiEvent> = mutableListOf()
            val handledBadgeCountEvents: MutableList<SdkApiEvent> = mutableListOf()
            val testAppEventListener: EmarsysSdkEventListener = { handledAppEvents.add(it) }
            val testBadgeCountEventListener: EmarsysSdkEventListener =
                { handledBadgeCountEvents.add(it) }
            val throwingListener: EmarsysSdkEventListener = { throw testError }

            emitter.on(SDK_APP_EVENT, testAppEventListener)
            emitter.on(SDK_APP_EVENT, throwingListener)
            emitter.on(SDK_BADGE_COUNT_EVENT, testBadgeCountEventListener)

            sdkOutboundEventFLow.emit(testAppEvent1)
            sdkOutboundEventFLow.emit(testAppEvent2)
            sdkOutboundEventFLow.emit(testBadgeCountEvent)

            advanceUntilIdle()

            handledAppEvents.size shouldBe 2
            handledBadgeCountEvents.size shouldBe 1

            val event1 = handledAppEvents.find { it.id === ID } as SdkApiAppEvent
            event1.type shouldBe SDK_APP_EVENT
            event1.name shouldBe NAME
            JSON.stringify(event1.attributes) shouldBe testAttributes.toString()

            val event2 = handledAppEvents.find { it.id === ID2 } as SdkApiAppEvent
            event2.type shouldBe SDK_APP_EVENT
            event2.name shouldBe NAME2
            JSON.stringify(event2.attributes) shouldBe testAttributes2.toString()

            val badgeEvent = handledBadgeCountEvents[0] as SdkApiBadgeCountEvent
            badgeEvent.id shouldBe testBadgeCountEvent.id
            badgeEvent.badgeCount shouldBe testBadgeCountEvent.badgeCount
            badgeEvent.method shouldBe testBadgeCountEvent.method

            verifySuspend(VerifyMode.exactly(2)) {
                mockLogger.error(
                    "Listener invocation failed for event type: $SDK_APP_EVENT",
                    testError,
                    false
                )
            }
        }

    @Test
    fun once_shouldInvokeTheListener_onlyOnce_andSendDebugLog() =
        runTest {
            val emitter = createEmitter(backgroundScope)
            val testAppEvent1 =
                SdkEvent.External.Api.AppEvent(ID, NAME, testAttributes, testTimestamp)
            val testAppEvent2 =
                SdkEvent.External.Api.AppEvent(ID2, NAME2, testAttributes2, testTimestamp)
            val handledAppEvents: MutableList<SdkApiEvent> = mutableListOf()
            val testAppEventListener: EmarsysSdkEventListener = { handledAppEvents.add(it) }

            emitter.once(SDK_APP_EVENT, testAppEventListener)

            sdkOutboundEventFLow.emit(testAppEvent1)
            sdkOutboundEventFLow.emit(testAppEvent2)

            advanceUntilIdle()

            handledAppEvents.size shouldBe 1

            val event1 = handledAppEvents.find { it.id === ID } as SdkApiAppEvent
            event1.type shouldBe SDK_APP_EVENT
            event1.name shouldBe NAME
            JSON.stringify(event1.attributes) shouldBe testAttributes.toString()

            handledAppEvents.find { it.id === ID2 } shouldBe null

            verifySuspend { mockLogger.debug("Registering once listener for event type: $SDK_APP_EVENT") }
        }

    @Test
    fun off_shouldRemoveTheListener_fromTheRegisteredListeners_andSendDebugLog() =
        runTest {
            val emitter = createEmitter(backgroundScope)
            val handledAppEvents: MutableList<SdkApiEvent> = mutableListOf()
            val handledAppEvents2: MutableList<SdkApiEvent> = mutableListOf()
            val testAppEventListener: EmarsysSdkEventListener = { handledAppEvents.add(it) }
            val testAppEventListener2: EmarsysSdkEventListener = { handledAppEvents2.add(it) }

            emitter.on(SDK_APP_EVENT, testAppEventListener)
            emitter.on(SDK_APP_EVENT, testAppEventListener2)

            emitter.off(SDK_APP_EVENT, testAppEventListener)

            sdkOutboundEventFLow.emit(testAppEvent1)

            advanceUntilIdle()

            handledAppEvents.size shouldBe 0
            handledAppEvents2.size shouldBe 1

            verifySuspend { mockLogger.debug("Removing listener for event type: $SDK_APP_EVENT") }
        }

    @Test
    fun off_shouldStopCollecting_ifTheLastListenerIsRemoved() =
        runTest {
            val handledAppEvents: MutableList<SdkApiEvent> = mutableListOf()
            val testAppEventListener: EmarsysSdkEventListener = { handledAppEvents.add(it) }
            val emitter = createEmitter(backgroundScope)

            emitter.on(SDK_APP_EVENT, testAppEventListener)
            sdkOutboundEventFLow.emit(testAppEvent1)

            advanceUntilIdle()
            handledAppEvents.size shouldBe 1

            emitter.off(SDK_APP_EVENT, testAppEventListener)

            sdkOutboundEventFLow.emit(testAppEvent1)

            advanceUntilIdle()

            handledAppEvents.size shouldBe 1
        }

    @Test
    fun removeAllListeners_shouldClearAllRegisteredListeners_sendDebugLog_andStopCollecting_() =
        runTest {
            val handledAppEvents: MutableList<SdkApiEvent> = mutableListOf()
            val handledBadgeCountEvents: MutableList<SdkApiEvent> = mutableListOf()
            val testAppEventListener: EmarsysSdkEventListener = { handledAppEvents.add(it) }
            val testBadgeCountEventListener: EmarsysSdkEventListener =
                { handledBadgeCountEvents.add(it) }
            val emitter = createEmitter(backgroundScope)

            emitter.on(SDK_APP_EVENT, testAppEventListener)
            emitter.on(SDK_BADGE_COUNT_EVENT, testBadgeCountEventListener)

            sdkOutboundEventFLow.emit(testAppEvent1)
            sdkOutboundEventFLow.emit(testBadgeCountEvent)

            advanceUntilIdle()
            handledAppEvents.size shouldBe 1
            handledBadgeCountEvents.size shouldBe 1

            emitter.removeAllListeners()

            sdkOutboundEventFLow.emit(testAppEvent1)
            sdkOutboundEventFLow.emit(testBadgeCountEvent)

            advanceUntilIdle()

            handledAppEvents.size shouldBe 1
            handledBadgeCountEvents.size shouldBe 1

            verifySuspend { mockLogger.debug("Registering all listeners.") }
        }
}