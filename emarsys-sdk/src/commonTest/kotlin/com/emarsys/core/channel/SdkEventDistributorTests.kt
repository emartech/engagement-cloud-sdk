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
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
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
    fun distributor_shouldSendEvents_toEventsDao_whenConnection_isOffline() = runTest {
        val testConnectionState = MutableStateFlow(false)
        val eventDistributor = createEventDistributor(testConnectionState, sdkContext)
        eventDistributor.register()
        advanceUntilIdle()

        val processedEvent = backgroundScope.async {
            sdkEventFlow.first()
        }

        val testEvent = SdkEvent.External.Custom(id = "testId", name = "testEventName")
        eventDistributor.registerEvent(testEvent)

        processedEvent.await() shouldBe testEvent
        verifySuspend { mockEventsDao.insertEvent(testEvent) }
    }

    @Test
    fun distributor_shouldHandleException_whenPersistingEventFails() =
        runTest {
            val testConnectionState = MutableStateFlow(false)
            val testException = Exception("testException")
            val testEvent = SdkEvent.External.Custom(id = "testId", name = "testEventName")
            everySuspend { mockEventsDao.insertEvent(testEvent) } throws testException
            val eventDistributor = createEventDistributor(testConnectionState, sdkContext)
            eventDistributor.register()
            advanceUntilIdle()

            val processedEvent = backgroundScope.async {
                sdkEventFlow.first()
            }

            eventDistributor.registerEvent(testEvent)

            processedEvent.await() shouldBe testEvent
            shouldThrow<Exception> { mockEventsDao.insertEvent(testEvent) }
            verifySuspend {
                mockSdkLogger.error(
                    "SdkEventDistributor - persistSdkEvent",
                    testException,
                    any()
                )
            }
        }

    @Test
    fun distributor_shouldSendEvents_toOnlineFlow_whenConnection_isOnline() = runTest {
        everySuspend { mockEventsDao.getEvents() } returns flowOf()
        val testEvent = SdkEvent.External.Custom(id = "testId", name = "testEventName")
        val testEvent2 = SdkEvent.External.Custom(id = "testId2", name = "testEventName2")
        val testEvents = listOf(testEvent, testEvent2)
        val testConnectionState = MutableStateFlow(true)
        val eventDistributor = createEventDistributor(testConnectionState, sdkContext)
        eventDistributor.register()
        advanceUntilIdle()

        val emittedEvents = backgroundScope.async {
            eventDistributor.onlineEvents.take(2).toList()
        }

        testEvents.forEach {
            eventDistributor.registerEvent(it)
        }

        verifySuspend(VerifyMode.exactly(0)) {
            mockEventsDao.insertEvent(testEvent)
            mockEventsDao.insertEvent(testEvent2)
        }
        emittedEvents.await() shouldBe testEvents
    }

    @Test
    fun distributor_shouldHandleException_whenGettingEventsFails() = runTest {
        val testException = Exception("testException")
        everySuspend { mockEventsDao.getEvents() } throws testException
        val testEvent = SdkEvent.External.Custom(id = "testId", name = "testEventName")
        val testEvent2 = SdkEvent.External.Custom(id = "testId2", name = "testEventName2")
        val testEvents = listOf(testEvent, testEvent2)
        val testConnectionState = MutableStateFlow(true)
        val eventDistributor = createEventDistributor(testConnectionState, sdkContext)
        eventDistributor.register()
        advanceUntilIdle()

        testEvents.forEach {
            eventDistributor.registerEvent(it)
        }

        verifySuspend(VerifyMode.exactly(0)) { mockEventsDao.insertEvent(testEvent) }
        shouldThrow<Exception> { mockEventsDao.getEvents() }
        verifySuspend {
            mockSdkLogger.error(
                "SdkEventDistributor - watchConnectionStatus",
                testException
            )
        }
    }

    @Test
    fun distributor_shouldFetchEvents_fromDao_andSendThemFirst_whenConnection_comesBackOnline_andThenDeleteThemFromDb() =
        runTest {
            val testEvent = SdkEvent.External.Custom(id = "testId", name = "testEventName")
            val testEvent2 = SdkEvent.External.Custom(id = "testId2", name = "testEventName2")
            val testEvent3 = SdkEvent.External.Custom(id = "testId3", name = "testEventName3")
            val testEvent4 = SdkEvent.External.Custom(id = "testId4", name = "testEventName4")
            everySuspend { mockEventsDao.getEvents() } returns flowOf(
                testEvent,
                testEvent3
            )
            val testEvents = listOf(testEvent, testEvent3, testEvent2, testEvent4)
            val testConnectionState = MutableStateFlow(false)
            val eventDistributor = createEventDistributor(testConnectionState, sdkContext)
            eventDistributor.register()
            advanceUntilIdle()

            val emittedEvents = backgroundScope.async {
                eventDistributor.onlineEvents.take(4).toList()
            }

            testConnectionState.emit(true)

            eventDistributor.registerEvent(testEvent2)
            eventDistributor.registerEvent(testEvent4)

            verifySuspend(VerifyMode.exactly(0)) { mockEventsDao.insertEvent(testEvent) }
            emittedEvents.await() shouldBe testEvents
            verifySuspend {
                mockEventsDao.removeEvent(testEvent)
                mockEventsDao.removeEvent(testEvent3)
            }
        }

    @Test
    fun registerEvent_shouldEmitSdkEvent_toSdkEventFlow() = runTest {
        val testEvent = SdkEvent.External.Custom(id = "testId", name = "testEventName")
        val eventDistributor = createEventDistributor(MutableStateFlow(false), sdkContext)

        val emittedEvents = backgroundScope.async {
            sdkEventFlow.take(1).toList()
        }

        eventDistributor.registerEvent(testEvent)

        emittedEvents.await() shouldBe listOf(testEvent)
    }

    @OptIn(FlowPreview::class)
    @Test
    fun distributor_shouldPauseOnlineEvents() =
        runTest {
            everySuspend { mockEventsDao.getEvents() } returns flowOf()
            val eventDistributor =
                createEventDistributor(connectionState = MutableStateFlow(true), sdkContext)
            val testEvent = SdkEvent.External.Custom(id = "testId", name = "testEventName")
            sdkContext.setSdkState(SdkState.onHold)

            eventDistributor.register()
            advanceUntilIdle()

            eventDistributor.registerEvent(testEvent)

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

            eventDistributor.registerEvent(testEvent)

            onHoldCollected1.await() shouldBe null
            onHoldCollected2.await() shouldBe null

            val emittedOnlineEventsWhenActive = backgroundScope.async {
                eventDistributor.onlineEvents.take(1).toList()
            }

            sdkContext.setSdkState(SdkState.active)
            advanceUntilIdle()

            emittedOnlineEventsWhenActive.await() shouldBe listOf(testEvent)
        }

    private suspend fun createEventDistributor(
        connectionState: MutableStateFlow<Boolean>,
        sdkContext: SdkContextApi
    ): SdkEventDistributor {
        sdkContext.setSdkState(SdkState.active)
        return SdkEventDistributor(
            sdkEventFlow,
            connectionState,
            sdkContext,
            mockEventsDao,
            sdkDispatcher,
            mockSdkLogger
        )
    }
}