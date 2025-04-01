package com.emarsys.core.channel

import com.emarsys.api.SdkState
import com.emarsys.context.SdkContext
import com.emarsys.context.SdkContextApi
import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.log.LogLevel
import com.emarsys.core.log.Logger
import com.emarsys.networking.clients.event.model.SdkEvent
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class SdkEventDistributorTests {
    private lateinit var sdkEventFlow: MutableSharedFlow<SdkEvent>
    private lateinit var sdkContext: SdkContextApi
    private lateinit var sdkDispatcher: CoroutineDispatcher
    private lateinit var mockEventsDao: EventsDaoApi
    private lateinit var mockSdkLogger: Logger

    @BeforeTest
    fun setup() {
        val mainDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(mainDispatcher)
        sdkEventFlow = MutableSharedFlow(replay = 100, extraBufferCapacity = Channel.UNLIMITED)
        sdkDispatcher = StandardTestDispatcher()
        sdkContext =
            SdkContext(
                sdkDispatcher,
                mainDispatcher,
                defaultUrls = mock(MockMode.autofill),
                LogLevel.Debug,
                mutableSetOf()
            )
        mockEventsDao = mock(MockMode.autofill)
        mockSdkLogger = mock(MockMode.autofill)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun distributor_shouldSendEvents_toOnlineFlow_whenConnection_isOnline_andSdkState_isActive() =
        runTest {
            everySuspend { mockEventsDao.getEvents() } returns flowOf()
            val testEvent = SdkEvent.External.Custom(id = "testId", name = "testEventName")
            val testEvent2 = SdkEvent.External.Custom(id = "testId2", name = "testEventName2")
            val testEvents = listOf(testEvent, testEvent2)
            val testConnectionState = MutableStateFlow(true)
            sdkContext.setSdkState(SdkState.active)
            val eventDistributor = createEventDistributor(testConnectionState, sdkContext)

            val emittedEvents = backgroundScope.async {
                eventDistributor.onlineEvents.take(2).toList()
            }

            testEvents.forEach {
                eventDistributor.registerEvent(it)
            }

            emittedEvents.await() shouldBe testEvents
        }

    @Test
    fun registerEvent_shouldPersistEventsToDb_andEmitSdkEvent_toSdkEventFlow() = runTest {
        val testEvent = SdkEvent.External.Custom(id = "testId", name = "testEventName")
        val eventDistributor = createEventDistributor(MutableStateFlow(false), sdkContext)

        val emittedEvents = backgroundScope.async {
            sdkEventFlow.take(1).toList()
        }

        eventDistributor.registerEvent(testEvent)

        emittedEvents.await() shouldBe listOf(testEvent)
        verifySuspend {
            mockEventsDao.insertEvent(testEvent)
        }
    }

    @Test
    fun registerEvent_shouldLogError_andNotEmitIntoSdkEventFlow_whenPersistingEventFails() =
        runTest {
            val testEvent = SdkEvent.External.Custom(id = "testId", name = "testEventName")
            val testException = RuntimeException("DB error")
            everySuspend { mockEventsDao.insertEvent(testEvent) } throws testException
            val eventDistributor = createEventDistributor(MutableStateFlow(false), sdkContext)

            eventDistributor.registerEvent(testEvent)

            verifySuspend {
                mockEventsDao.insertEvent(testEvent)
                mockSdkLogger.error(any(), testException, any())
            }
        }

    @Test
    fun emitEvent_shouldEmitSdkEvent_toSdkEventFlow() = runTest {
        val testEvent = SdkEvent.External.Custom(id = "testId", name = "testEventName")
        val eventDistributor = createEventDistributor(MutableStateFlow(false), sdkContext)

        val emittedEvents = backgroundScope.async {
            sdkEventFlow.take(1).toList()
        }

        eventDistributor.emitEvent(testEvent)

        emittedEvents.await() shouldBe listOf(testEvent)
    }

    @Test
    fun emitEvent_shouldLog_andSwallowException_whenEmitFails() = runTest {
        val testEvent = SdkEvent.External.Custom(id = "testId", name = "testEventName")
        val mockFlow = mock<MutableSharedFlow<SdkEvent>>()
        everySuspend { mockFlow.emit(testEvent) } throws Exception("Emit failed")
        val eventDistributor = createEventDistributor(MutableStateFlow(false), sdkContext, mockFlow)

        eventDistributor.emitEvent(testEvent)

        verifySuspend {
            mockFlow.emit(testEvent)
            mockSdkLogger.error(any<String>(), any<Exception>(), any())
        }
    }

    @OptIn(FlowPreview::class)
    @Test
    fun distributor_shouldPauseOnlineEvents_whenSdkStateIsOnHold_andConnectionIsOnline_withTwoCollectors() =
        runTest {
            everySuspend { mockEventsDao.getEvents() } returns flowOf()
            val eventDistributor =
                createEventDistributor(connectionState = MutableStateFlow(true), sdkContext)
            val testEvent1 = SdkEvent.External.Custom(id = "testId", name = "testEventName")
            val testEvent2 = SdkEvent.External.Custom(id = "testId1", name = "testEventName")
            sdkContext.setSdkState(SdkState.onHold)

            val onHoldCollected1 = backgroundScope.async {
                try {
                    eventDistributor.onlineEvents.timeout(500.milliseconds).firstOrNull()
                } catch (e: TimeoutCancellationException) {
                    null
                }
            }
            val onHoldCollected2 = backgroundScope.async {
                try {
                    eventDistributor.onlineEvents.timeout(500.milliseconds).firstOrNull()
                } catch (e: TimeoutCancellationException) {
                    null
                }
            }

            eventDistributor.registerEvent(testEvent1)
            eventDistributor.registerEvent(testEvent2)

            onHoldCollected1.await() shouldBe null
            onHoldCollected2.await() shouldBe null

            val emittedOnlineEventsWhenActive = backgroundScope.async {
                eventDistributor.onlineEvents.take(2).toList()
            }

            sdkContext.setSdkState(SdkState.active)
            advanceUntilIdle()

            emittedOnlineEventsWhenActive.await() shouldBe listOf(testEvent1, testEvent2)
        }


    @OptIn(FlowPreview::class)
    @Test
    fun distributor_shouldPauseOnlineEvents_whenSdkStateIsActive_andConnection_isOffline() =
        runTest {
            everySuspend { mockEventsDao.getEvents() } returns flowOf()
            val connectionState = MutableStateFlow(false)
            val eventDistributor =
                createEventDistributor(connectionState = connectionState, sdkContext)
            val testEvent = SdkEvent.External.Custom(id = "testId", name = "testEventName")
            sdkContext.setSdkState(SdkState.active)

            val offlineCollected = backgroundScope.async {
                try {
                    eventDistributor.onlineEvents.timeout(500.milliseconds).firstOrNull()
                } catch (e: TimeoutCancellationException) {
                    null
                }
            }

            eventDistributor.registerEvent(testEvent)

            offlineCollected.await() shouldBe null

            val emittedOnlineEventsWhenOnline = backgroundScope.async {
                eventDistributor.onlineEvents.take(1).toList()
            }

            connectionState.value = true
            advanceUntilIdle()

            emittedOnlineEventsWhenOnline.await() shouldBe listOf(testEvent)
        }

    @OptIn(FlowPreview::class)
    @Test
    fun distributor_shouldPauseOnlineEvents_whenSdkStateIsOnHold_andConnection_isOffline() =
        runTest {
            everySuspend { mockEventsDao.getEvents() } returns flowOf()
            val connectionState = MutableStateFlow(false)
            val eventDistributor =
                createEventDistributor(connectionState = connectionState, sdkContext)
            val testEvent = SdkEvent.External.Custom(id = "testId", name = "testEventName")
            sdkContext.setSdkState(SdkState.onHold)

            val offlineCollected = backgroundScope.async {
                try {
                    eventDistributor.onlineEvents.timeout(500.milliseconds).firstOrNull()
                } catch (e: TimeoutCancellationException) {
                    null
                }
            }

            eventDistributor.registerEvent(testEvent)

            offlineCollected.await() shouldBe null

            val emittedOnlineEventsWhenOnline = backgroundScope.async {
                eventDistributor.onlineEvents.take(1).toList()
            }

            connectionState.value = true
            advanceUntilIdle()

            delay(5000)

            sdkContext.setSdkState(SdkState.active)

            emittedOnlineEventsWhenOnline.await() shouldBe listOf(testEvent)
        }

    private suspend fun createEventDistributor(
        connectionState: MutableStateFlow<Boolean>,
        sdkContext: SdkContextApi,
        eventFlow: MutableSharedFlow<SdkEvent> = sdkEventFlow,
    ): SdkEventDistributor {
        sdkContext.setSdkState(SdkState.active)
        return SdkEventDistributor(
            eventFlow,
            connectionState,
            sdkContext,
            mockEventsDao,
            mockSdkLogger
        )
    }
}