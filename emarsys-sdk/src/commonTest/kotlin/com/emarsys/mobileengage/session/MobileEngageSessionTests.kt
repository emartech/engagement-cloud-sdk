package com.emarsys.mobileengage.session

import com.emarsys.core.actions.LifecycleEvent
import com.emarsys.core.providers.Provider
import com.emarsys.core.session.SessionContext
import com.emarsys.core.session.SessionId
import com.emarsys.networking.clients.event.EventClientApi
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
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MobileEngageSessionTests : TestsWithMocks() {
    private companion object {
        const val SESSION_START = 123456789L
        val SESSION_ID = SessionId("testSessionId")
    }

    override fun setUpMocks() = injectMocks(mocker)

    @Mock
    lateinit var mockTimestampProvider: Provider<Instant>

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

        every { mockTimestampProvider.provide() } returns Instant.fromEpochMilliseconds(
            SESSION_START
        )
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

}