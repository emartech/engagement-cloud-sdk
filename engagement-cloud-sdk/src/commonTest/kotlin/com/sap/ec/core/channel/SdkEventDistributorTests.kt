package com.sap.ec.core.channel

import com.sap.ec.api.SdkState
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.db.events.EventsDaoApi
import com.sap.ec.core.log.LogEventRegistryApi
import com.sap.ec.core.log.LogLevel
import com.sap.ec.core.log.Logger
import com.sap.ec.event.SdkEvent
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
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
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class SdkEventDistributorTests {
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var sdkDispatcher: CoroutineDispatcher
    private lateinit var mockEventsDao: EventsDaoApi
    private lateinit var mockLogEventRegistryApi: LogEventRegistryApi
    private lateinit var applicationScope: CoroutineScope
    private lateinit var mockSdkLogger: Logger

    @BeforeTest
    fun setup() {
        val mainDispatcher = StandardTestDispatcher()
        applicationScope = CoroutineScope(mainDispatcher)
        Dispatchers.setMain(mainDispatcher)
        sdkDispatcher = StandardTestDispatcher()
        mockSdkContext = mock(MockMode.autofill)
        mockEventsDao = mock(MockMode.autofill)
        mockLogEventRegistryApi = mock(MockMode.autofill)
        mockSdkLogger = mock(MockMode.autofill)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun register_shouldCollectLogEvents_andRegisterThem() = runTest {
        val logEvent = SdkEvent.Internal.Sdk.Log(
            level = LogLevel.Info,
            attributes = buildJsonObject {
                put("message", "Test log message")
            }
        )
        val logEventsFlow = MutableSharedFlow<SdkEvent.Internal.LogEvent>()
        every { mockLogEventRegistryApi.logEvents } returns logEventsFlow

        val sdkEventDistributor = createEventDistributor(
            MutableStateFlow(true),
            mockSdkContext
        )

        val emittedEvents = backgroundScope.async {
            sdkEventDistributor.sdkEventFlow.take(1).toList()
        }

        val emittedLogEvents = backgroundScope.async {
            sdkEventDistributor.logEvents.take(1).toList()
        }

        sdkEventDistributor.register()
        advanceUntilIdle()

        logEventsFlow.emit(logEvent)
        advanceUntilIdle()

        emittedEvents.await() shouldBe listOf(logEvent)
        verifySuspend { mockEventsDao.insertEvent(logEvent) }

        emittedLogEvents.await() shouldBe listOf(logEvent)
    }

    @Test
    fun distributor_shouldSendAllEvents_toOnlineFlow_whenConnection_isOnline_andSdkState_isActive() =
        runTest {
            everySuspend { mockEventsDao.getEvents() } returns flowOf()
            val testEvent = SdkEvent.External.Custom(id = "testId", name = "testEventName")
            val testEvent2 = SdkEvent.Internal.Sdk.AppStart(id = "testId2")
            val testEvents = listOf(testEvent, testEvent2)
            val testConnectionState = MutableStateFlow(true)
            val sdkEventDistributor = createEventDistributor(testConnectionState, mockSdkContext)
            every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Active)

            val emittedEvents = backgroundScope.async {
                sdkEventDistributor.onlineSdkEvents.take(2).toList()
            }

            testEvents.forEach {
                sdkEventDistributor.registerEvent(it)
            }

            emittedEvents.await() shouldContainExactlyInAnyOrder testEvents
        }

    @Test
    fun distributor_shouldSendEvent_toOnlineFlow_whenConnection_isOnline_andSdkState_isOnHold_andEventIsSetupFlowEvent() =
        runTest {
            everySuspend { mockEventsDao.getEvents() } returns flowOf()
            val testEvent = SdkEvent.External.Custom(id = "testId", name = "testEventName")
            val testSetupFlowEvent = SdkEvent.Internal.Sdk.AppStart(id = "testId2")
            val testConnectionState = MutableStateFlow(true)
            val sdkEventDistributor = createEventDistributor(testConnectionState, mockSdkContext)
            every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.OnHold)

            val emittedEvents = backgroundScope.async {
                sdkEventDistributor.onlineSdkEvents.take(1).toList()
            }

            sdkEventDistributor.registerEvent(testEvent)
            sdkEventDistributor.registerEvent(testSetupFlowEvent)

            emittedEvents.await() shouldBe listOf(testSetupFlowEvent)
        }

    @Test
    fun registerAndStoreEvent_shouldPersistOnlineEventsToDb_andEmitSdkEvent_toSdkEventFlow() =
        runTest {
            val testEvent = SdkEvent.External.Custom(id = "testId", name = "testEventName")
            val sdkEventDistributor =
                createEventDistributor(MutableStateFlow(false), mockSdkContext)

            val emittedEvents = backgroundScope.async {
                sdkEventDistributor.sdkEventFlow.take(1).toList()
            }

            sdkEventDistributor.registerEvent(testEvent)

            emittedEvents.await() shouldBe listOf(testEvent)
            verifySuspend {
                mockEventsDao.insertEvent(testEvent)
            }
        }

    @Test
    fun registerAndStoreEvent_shouldNotPersistNotOnlineEvents_andEmitSdkEvent_toSdkEventFlow() =
        runTest {
            val testEvent = SdkEvent.External.Api.BadgeCountEvent(
                badgeCount = 1234,
                method = "set"
            )
            val sdkEventDistributor =
                createEventDistributor(MutableStateFlow(false), mockSdkContext)

            val emittedEvents = backgroundScope.async {
                sdkEventDistributor.sdkEventFlow.take(1).toList()
            }

            sdkEventDistributor.registerEvent(testEvent)

            emittedEvents.await() shouldBe listOf(testEvent)
            verifySuspend(VerifyMode.exactly(0)) {
                mockEventsDao.insertEvent(testEvent)
            }
        }

    @Test
    fun registerAndStoreEvent_shouldLogError_andNotEmitIntoSdkEventFlow_whenPersistingEventFails() =
        runTest {
            val testEvent = SdkEvent.External.Custom(id = "testId", name = "testEventName")
            val testException = RuntimeException("DB error")
            everySuspend { mockEventsDao.insertEvent(testEvent) } throws testException
            val sdkEventDistributor =
                createEventDistributor(MutableStateFlow(false), mockSdkContext)

            sdkEventDistributor.registerEvent(testEvent)

            verifySuspend {
                mockEventsDao.insertEvent(testEvent)
                mockSdkLogger.error(any(), testException, any(), true)
            }
        }

    @Test
    fun registerAndStoreLogEvent_shouldLogError_andNotEmitIntoSdkEventFlow_whenPersistingEventFails() =
        runTest {
            val testEvent = SdkEvent.External.Custom(id = "testId", name = "testEventName")
            val testException = RuntimeException("DB error")
            everySuspend { mockEventsDao.insertEvent(testEvent) } throws testException
            val sdkEventDistributor =
                createEventDistributor(MutableStateFlow(false), mockSdkContext)

            sdkEventDistributor.registerEvent(testEvent)

            verifySuspend {
                mockEventsDao.insertEvent(testEvent)
            }
        }

    @Test
    fun emitEvent_shouldEmitSdkEvent_toSdkEventFlow() = runTest {
        val testEvent = SdkEvent.External.Custom(id = "testId", name = "testEventName")
        val sdkEventDistributor = createEventDistributor(MutableStateFlow(false), mockSdkContext)

        val emittedEvents = backgroundScope.async {
            sdkEventDistributor.sdkEventFlow.take(1).toList()
        }

        sdkEventDistributor.emitEvent(testEvent)

        emittedEvents.await() shouldBe listOf(testEvent)
    }

    @OptIn(FlowPreview::class)
    @Test
    fun distributor_shouldPauseOnlineEvents_whenSdkStateIsActive_andConnection_isOffline() =
        runTest {
            everySuspend { mockEventsDao.getEvents() } returns flowOf()
            val connectionState = MutableStateFlow(false)
            val sdkEventDistributor =
                createEventDistributor(connectionState = connectionState, mockSdkContext)
            val testEvent = SdkEvent.External.Custom(id = "testId", name = "testEventName")
            every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Active)

            val offlineCollected = backgroundScope.async {
                try {
                    sdkEventDistributor.onlineSdkEvents.timeout(500.milliseconds).firstOrNull()
                } catch (e: TimeoutCancellationException) {
                    null
                }
            }

            sdkEventDistributor.registerEvent(testEvent)

            offlineCollected.await() shouldBe null

            val emittedOnlineEventsWhenOnline = backgroundScope.async {
                sdkEventDistributor.onlineSdkEvents.take(1).toList()
            }

            connectionState.value = true
            advanceUntilIdle()

            emittedOnlineEventsWhenOnline.await() shouldBe listOf(testEvent)
        }

    @OptIn(FlowPreview::class)
    @Test
    fun distributor_shouldPauseOnlineEvents_whenSdkStateIsOnHold_andConnection_isOffline() =
        runTest {
            val sdkStateFlow = MutableStateFlow(SdkState.OnHold)
            everySuspend { mockEventsDao.getEvents() } returns flowOf()
            val connectionState = MutableStateFlow(false)
            val sdkEventDistributor =
                createEventDistributor(connectionState = connectionState, mockSdkContext)
            val testEvent = SdkEvent.External.Custom(id = "testId", name = "testEventName")
            every { mockSdkContext.currentSdkState } returns sdkStateFlow

            val offlineCollected = backgroundScope.async {
                try {
                    sdkEventDistributor.onlineSdkEvents.timeout(500.milliseconds).firstOrNull()
                } catch (e: TimeoutCancellationException) {
                    null
                }
            }

            sdkEventDistributor.registerEvent(testEvent)

            offlineCollected.await() shouldBe null

            val emittedOnlineEventsWhenOnline = backgroundScope.async {
                sdkEventDistributor.onlineSdkEvents.take(1).toList()
            }

            connectionState.value = true
            advanceUntilIdle()

            delay(5000)

            sdkStateFlow.value = SdkState.Active

            emittedOnlineEventsWhenOnline.await() shouldBe listOf(testEvent)
        }

    private fun createEventDistributor(
        connectionState: MutableStateFlow<Boolean>,
        sdkContext: SdkContextApi,
    ): SdkEventDistributor {
        return SdkEventDistributor(
            connectionState,
            sdkContext,
            mockEventsDao,
            mockLogEventRegistryApi,
            applicationScope,
            mockSdkLogger,
        )
    }
}