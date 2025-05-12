package com.emarsys.core.channel

import com.emarsys.api.SdkState
import com.emarsys.context.SdkContext
import com.emarsys.context.SdkContextApi
import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.log.LogLevel
import com.emarsys.core.log.Logger
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.di.SdkKoinIsolationContext.koin
import com.emarsys.fake.FakeStringStorage
import com.emarsys.networking.clients.event.model.SdkEvent
import com.emarsys.util.JsonUtil
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
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
import kotlinx.serialization.json.Json
import org.koin.core.Koin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.test.KoinTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class SdkEventDistributorTests: KoinTest {

    override fun getKoin(): Koin = koin

    private lateinit var testModule: Module

    private lateinit var sdkContext: SdkContextApi
    private lateinit var sdkDispatcher: CoroutineDispatcher
    private lateinit var mockEventsDao: EventsDaoApi
    private lateinit var mockSdkLogger: Logger

    @BeforeTest
    fun setup() {
        testModule = module {
            single<StringStorageApi> { FakeStringStorage() }
            single<Json> { JsonUtil.json }
        }
        koin.loadModules(listOf(testModule))

        val mainDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(mainDispatcher)
        sdkDispatcher = StandardTestDispatcher()
        sdkContext =
            SdkContext(
                sdkDispatcher,
                mainDispatcher,
                defaultUrls = mock(MockMode.autofill),
                LogLevel.Debug,
                mutableSetOf(),
                logBreadcrumbsQueueSize = 10
            )
        mockEventsDao = mock(MockMode.autofill)
        mockSdkLogger = mock(MockMode.autofill)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        koin.unloadModules(listOf(testModule))
    }

    @Test
    fun distributor_shouldSendAllEvents_toOnlineFlow_whenConnection_isOnline_andSdkState_isActive() =
        runTest {
            everySuspend { mockEventsDao.getEvents() } returns flowOf()
            val testEvent = SdkEvent.External.Custom(id = "testId", name = "testEventName")
            val testEvent2 = SdkEvent.Internal.Sdk.AppStart(id = "testId2")
            val testEvents = listOf(testEvent, testEvent2)
            val testConnectionState = MutableStateFlow(true)
            val sdkEventDistributor = createEventDistributor(testConnectionState, sdkContext)
            sdkContext.setSdkState(SdkState.active)

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
            val sdkEventDistributor = createEventDistributor(testConnectionState, sdkContext)
            sdkContext.setSdkState(SdkState.onHold)

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
            val sdkEventDistributor = createEventDistributor(MutableStateFlow(false), sdkContext)

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
                name = "testEventName",
                badgeCount = 1234,
                method = "set"
            )
            val sdkEventDistributor = createEventDistributor(MutableStateFlow(false), sdkContext)

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
            val sdkEventDistributor = createEventDistributor(MutableStateFlow(false), sdkContext)

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
            val sdkEventDistributor = createEventDistributor(MutableStateFlow(false), sdkContext)

            sdkEventDistributor.registerEvent(testEvent)

            verifySuspend {
                mockEventsDao.insertEvent(testEvent)
            }
        }

    @Test
    fun emitEvent_shouldEmitSdkEvent_toSdkEventFlow() = runTest {
        val testEvent = SdkEvent.External.Custom(id = "testId", name = "testEventName")
        val sdkEventDistributor = createEventDistributor(MutableStateFlow(false), sdkContext)

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
                createEventDistributor(connectionState = connectionState, sdkContext)
            val testEvent = SdkEvent.External.Custom(id = "testId", name = "testEventName")
            sdkContext.setSdkState(SdkState.active)

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
            everySuspend { mockEventsDao.getEvents() } returns flowOf()
            val connectionState = MutableStateFlow(false)
            val sdkEventDistributor =
                createEventDistributor(connectionState = connectionState, sdkContext)
            val testEvent = SdkEvent.External.Custom(id = "testId", name = "testEventName")
            sdkContext.setSdkState(SdkState.onHold)

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

            sdkContext.setSdkState(SdkState.active)

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
            mockSdkLogger
        )
    }
}