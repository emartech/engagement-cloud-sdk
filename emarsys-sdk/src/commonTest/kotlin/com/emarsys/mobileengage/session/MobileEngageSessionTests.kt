package com.emarsys.mobileengage.session

import com.emarsys.core.actions.LifecycleEvent
import com.emarsys.core.providers.Provider
import com.emarsys.core.session.SessionContext
import com.emarsys.core.session.SessionId
import com.emarsys.networking.clients.event.EventClientApi
import com.emarsys.networking.clients.event.model.Event
import com.emarsys.networking.clients.event.model.EventType
import com.emarsys.watchdog.lifecycle.LifecycleWatchDog
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Instant
import org.kodein.mock.Mock
import org.kodein.mock.Mocker
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MobileEngageSessionTests : TestsWithMocks() {
    private companion object {
        const val SESSION_START_UTC = "1970-01-02T10:17:36.789Z"
        const val SESSION_START = 123456789L
        const val SESSION_END = 123457789L
        const val SESSION_END_UTC = "1970-01-02T10:17:37.789Z"
        const val SESSION_DURATION = 1000L
        val SESSION_ID = SessionId("testSessionId")
        val sessionStartEvent = Event(
            EventType.INTERNAL,
            "session:start",
            null,
            SESSION_START_UTC
        )
        val sessionEndEvent = Event(
            EventType.INTERNAL,
            "session:end",
            mapOf("duration" to SESSION_DURATION.toString()),
            SESSION_END_UTC
        )
    }

    override fun setUpMocks() = injectMocks(mocker)

    @Mock
    lateinit var mockTimestampProvider: Provider<Instant>
    lateinit var timestampProviderMocker: Mocker.Every<Instant>

    @Mock
    lateinit var mockUuidProvider: Provider<String>

    lateinit var sessionContext: SessionContext

    @Mock
    lateinit var mockEventClient: EventClientApi

    private lateinit var sdkDispatcher: CoroutineDispatcher


    private lateinit var mobileEngageSession: MobileEngageSession

    init {
        Dispatchers.setMain(
            StandardTestDispatcher()
        )
    }

    @BeforeTest
    fun setUp() {
        sdkDispatcher = StandardTestDispatcher()
        sessionContext = SessionContext()

        timestampProviderMocker = every { mockTimestampProvider.provide() }
        timestampProviderMocker returns Instant.fromEpochMilliseconds(SESSION_START)
        every { mockUuidProvider.provide() } returns SESSION_ID.value

        mobileEngageSession = MobileEngageSession(
            mockTimestampProvider,
            mockUuidProvider,
            sessionContext,
            mockEventClient,
            sdkDispatcher
        )
    }

    @Test
    fun testSubscribe_shouldCallStartSession() = runTest {

        mobileEngageSession.subscribe(object : LifecycleWatchDog {
            override val lifecycleEvents: SharedFlow<LifecycleEvent>
                get() = flowOf(LifecycleEvent.OnForeground).shareIn(
                    scope = CoroutineScope(StandardTestDispatcher()),
                    started = SharingStarted.Eagerly
                )

            override suspend fun register() {}
        })
        advanceUntilIdle()
        sessionContext.sessionStart shouldBe SESSION_START
        sessionContext.sessionId shouldBe SESSION_ID
    }

    @Test
    fun testSubscribe_shouldCallEndSession() = runTest {
        val sharedFlow = MutableSharedFlow<LifecycleEvent>()
        mobileEngageSession.subscribe(object : LifecycleWatchDog {
            override val lifecycleEvents: SharedFlow<LifecycleEvent> = sharedFlow

            override suspend fun register() {}
        })
        sharedFlow.emit(LifecycleEvent.OnForeground)
        advanceUntilIdle()
        sessionContext.sessionStart shouldBe SESSION_START
        sessionContext.sessionId shouldBe SESSION_ID

        sharedFlow.emit(LifecycleEvent.OnBackground)
        advanceUntilIdle()
        sessionContext.sessionStart shouldBe null
        sessionContext.sessionId shouldBe null
    }


    @Test
    fun testStartSession_shouldTrackSessionStartEvent() = runTest {
        everySuspending { mockEventClient.registerEvent(sessionStartEvent) } returns Unit

        mobileEngageSession.subscribe(object : LifecycleWatchDog {
            override val lifecycleEvents: SharedFlow<LifecycleEvent>
                get() = flowOf(LifecycleEvent.OnForeground).shareIn(
                    scope = CoroutineScope(StandardTestDispatcher()),
                    started = SharingStarted.Eagerly
                )

            override suspend fun register() {}
        })
        advanceUntilIdle()
        verifyWithSuspend(exhaustive = false) { mockEventClient.registerEvent(sessionStartEvent) }
    }

    @Test
    fun testEndSession_shouldTrackSessionEndEvent() = runTest {
        everySuspending { mockEventClient.registerEvent(sessionEndEvent) } returns Unit
        everySuspending { mockEventClient.registerEvent(sessionStartEvent) } returns Unit
        val sharedFlow = MutableSharedFlow<LifecycleEvent>()
        mobileEngageSession.subscribe(object : LifecycleWatchDog {
            override val lifecycleEvents: SharedFlow<LifecycleEvent> = sharedFlow

            override suspend fun register() {}
        })
        sharedFlow.emit(LifecycleEvent.OnForeground)
        advanceUntilIdle()
        verifyWithSuspend(exhaustive = false) { mockEventClient.registerEvent(sessionStartEvent) }

        timestampProviderMocker returns Instant.fromEpochMilliseconds(SESSION_END)

        sharedFlow.emit(LifecycleEvent.OnBackground)

        advanceUntilIdle()
        verifyWithSuspend(exhaustive = false) { mockEventClient.registerEvent(sessionEndEvent) }
    }
}