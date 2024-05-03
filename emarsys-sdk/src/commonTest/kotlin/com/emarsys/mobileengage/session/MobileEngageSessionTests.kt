package com.emarsys.mobileengage.session

import com.emarsys.EmarsysConfig
import com.emarsys.context.SdkContextApi
import com.emarsys.core.actions.LifecycleEvent
import com.emarsys.core.log.Logger
import com.emarsys.core.providers.Provider
import com.emarsys.core.session.SessionContext
import com.emarsys.core.session.SessionId
import com.emarsys.networking.clients.event.EventClientApi
import com.emarsys.networking.clients.event.model.Event
import com.emarsys.networking.clients.event.model.EventType
import com.emarsys.watchdog.lifecycle.LifecycleWatchDog
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
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
        const val APPLICATION_CODE = "testApplicationCode"
        const val CONTACT_TOKEN = "testContactToken"
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

    @Mock
    lateinit var mockUuidProvider: Provider<String>

    @Mock
    lateinit var mockSdkLogger: Logger

    @Mock
    lateinit var mockEventClient: EventClientApi

    @Mock
    lateinit var mockSdkContext: SdkContextApi

    private lateinit var sessionContext: SessionContext

    private lateinit var timestampProviderMocker: Mocker.Every<Instant>

    private lateinit var uuidProviderMocker: Mocker.Every<String>

    private lateinit var eventClientStartEventRegistrationMocker: Mocker.EverySuspend<Unit>

    private lateinit var eventClientEndEventRegistrationMocker: Mocker.EverySuspend<Unit>

    private lateinit var sdkDispatcher: CoroutineDispatcher

    private lateinit var mobileEngageSession: MobileEngageSession

    init {
        Dispatchers.setMain(
            StandardTestDispatcher()
        )
    }

    @BeforeTest
    fun setUp() = runTest {
        sdkDispatcher = StandardTestDispatcher()

        sessionContext = SessionContext(
            contactToken = CONTACT_TOKEN,
            sessionId = SESSION_ID,
            sessionStart = SESSION_START
        )

        timestampProviderMocker = every { mockTimestampProvider.provide() }

        uuidProviderMocker = every { mockUuidProvider.provide() }
        uuidProviderMocker returns SESSION_ID.value

        eventClientStartEventRegistrationMocker =
            everySuspending { mockEventClient.registerEvent(sessionStartEvent) }
        eventClientStartEventRegistrationMocker returns Unit

        eventClientEndEventRegistrationMocker =
            everySuspending { mockEventClient.registerEvent(sessionEndEvent) }
        eventClientEndEventRegistrationMocker returns Unit

        every { mockSdkLogger.log(isAny(), isAny()) } returns Unit
        everySuspending { mockSdkLogger.debug(isAny()) } returns Unit
        everySuspending { mockSdkLogger.error(isAny()) } returns Unit

        mobileEngageSession = MobileEngageSession(
            mockTimestampProvider,
            mockUuidProvider,
            sessionContext,
            mockSdkContext,
            mockEventClient,
            sdkDispatcher,
            mockSdkLogger
        )
    }

    @Test
    fun testSubscribe_shouldCallStartSession() = runTest {
        sessionContext.contactToken = CONTACT_TOKEN
        sessionContext.sessionId = null
        sessionContext.sessionStart = null
        every { mockSdkContext.config } returns EmarsysConfig(applicationCode = APPLICATION_CODE)
        timestampProviderMocker returns Instant.fromEpochMilliseconds(SESSION_START)
        val sharedFlow = MutableSharedFlow<LifecycleEvent>()
        mobileEngageSession.subscribe(object : LifecycleWatchDog {
            override val lifecycleEvents: SharedFlow<LifecycleEvent> = sharedFlow

            override suspend fun register() {}
        })

        sharedFlow.emit(LifecycleEvent.OnForeground)
        advanceUntilIdle()

        sessionContext.sessionStart shouldBe SESSION_START
        sessionContext.sessionId shouldBe SESSION_ID
    }

    @Test
    fun testSubscribe_shouldCallEndSession() = runTest {
        everySuspending { mockEventClient.registerEvent(sessionStartEvent) } returns Unit
        everySuspending { mockEventClient.registerEvent(sessionEndEvent) } returns Unit
        every { mockSdkContext.config } returns EmarsysConfig(applicationCode = APPLICATION_CODE)
        timestampProviderMocker returns Instant.fromEpochMilliseconds(SESSION_START)
        val sharedFlow = MutableSharedFlow<LifecycleEvent>()
        mobileEngageSession.subscribe(object : LifecycleWatchDog {
            override val lifecycleEvents: SharedFlow<LifecycleEvent> = sharedFlow

            override suspend fun register() {}
        })

        sharedFlow.emit(LifecycleEvent.OnForeground)
        advanceUntilIdle()

        sessionContext.sessionStart shouldBe SESSION_START
        sessionContext.sessionId shouldBe SESSION_ID

        timestampProviderMocker returns Instant.fromEpochMilliseconds(SESSION_END)

        sharedFlow.emit(LifecycleEvent.OnBackground)
        advanceUntilIdle()

        sessionContext.sessionStart shouldBe null
        sessionContext.sessionId shouldBe null
    }


    @Test
    fun testStartSession_shouldTrackSessionStartEvent() = runTest {
        sessionContext.sessionId = null
        sessionContext.sessionStart = null
        every { mockSdkContext.config } returns EmarsysConfig(applicationCode = APPLICATION_CODE)
        timestampProviderMocker returns Instant.fromEpochMilliseconds(SESSION_START)

        mobileEngageSession.startSession()

        verifyWithSuspend(exhaustive = false) { mockEventClient.registerEvent(sessionStartEvent) }
    }

    @Test
    fun testEndSession_shouldTrackSessionEndEvent() = runTest {
        timestampProviderMocker returns Instant.fromEpochMilliseconds(SESSION_END)
        every { mockSdkContext.config } returns EmarsysConfig(applicationCode = APPLICATION_CODE)

        mobileEngageSession.endSession()

        verifyWithSuspend(exhaustive = false) { mockEventClient.registerEvent(sessionEndEvent) }
    }

    @Test
    fun testStartSession_shouldSetSession_evenWhenRegisteringEventFails() = runTest {
        sessionContext.sessionId = null
        sessionContext.sessionStart = null
        eventClientStartEventRegistrationMocker runs {
            throw RuntimeException(
                "uuid provider failed"
            )
        }
        timestampProviderMocker returns Instant.fromEpochMilliseconds(SESSION_START)
        every { mockSdkContext.config } returns EmarsysConfig(applicationCode = APPLICATION_CODE)

        mobileEngageSession.startSession()

        verifyWithSuspend(exhaustive = false) {
            threw<RuntimeException> { mockEventClient.registerEvent(sessionStartEvent) }
        }
        sessionContext.sessionStart shouldBe SESSION_START
        sessionContext.sessionId shouldBe SESSION_ID
    }

    @Test
    fun testStartSession_shouldNotDoAnything_whenApplicationCodeIsNull() = runTest {
        sessionContext.sessionId = null
        sessionContext.sessionStart = null
        every { mockSdkContext.config } returns EmarsysConfig(applicationCode = null)

        mobileEngageSession.startSession()

        verifySessionEventNotRegistered(sessionStartEvent)
        sessionContext.sessionId shouldBe null
        sessionContext.sessionStart shouldBe null
    }

    @Test
    fun testStartSession_shouldNotDoAnything_whenContactTokenIsNull() = runTest {
        sessionContext.contactToken = null
        every { mockSdkContext.config } returns EmarsysConfig(applicationCode = APPLICATION_CODE)

        mobileEngageSession.startSession()

        verifySessionEventNotRegistered(sessionStartEvent)
        sessionContext.sessionId shouldBe SESSION_ID
        sessionContext.sessionStart shouldBe SESSION_START
    }

    @Test
    fun testStartSession_shouldNotDoAnything_whenSessionIdIsNull() = runTest {
        sessionContext.sessionId = null
        every { mockSdkContext.config } returns EmarsysConfig(applicationCode = APPLICATION_CODE)

        mobileEngageSession.startSession()

        verifySessionEventNotRegistered(sessionStartEvent)
        sessionContext.sessionId shouldBe null
        sessionContext.sessionStart shouldBe SESSION_START
    }

    @Test
    fun testStartSession_shouldNotDoAnything_whenSessionStartIsNull() = runTest {
        sessionContext.sessionStart = null
        every { mockSdkContext.config } returns EmarsysConfig(applicationCode = APPLICATION_CODE)

        mobileEngageSession.startSession()

        verifySessionEventNotRegistered(sessionStartEvent)
        sessionContext.sessionId shouldBe SESSION_ID
        sessionContext.sessionStart shouldBe null
    }

    @Test
    fun testEndSession_shouldResetSession_evenWhenRegisteringEventFails() = runTest {
        timestampProviderMocker returns Instant.fromEpochMilliseconds(SESSION_END)
        eventClientEndEventRegistrationMocker runs {
            throw RuntimeException(
                "request failed"
            )
        }
        every { mockSdkContext.config } returns EmarsysConfig(applicationCode = APPLICATION_CODE)

        mobileEngageSession.endSession()

        verifyWithSuspend(exhaustive = false) {
            threw<RuntimeException> { mockEventClient.registerEvent(sessionEndEvent) }
        }
        sessionContext.sessionStart shouldBe null
        sessionContext.sessionId shouldBe null
    }

    @Test
    fun testEndSession_shouldNotDoAnything_whenSessionIdIsNull() = runTest {
        sessionContext.sessionId = null
        every { mockSdkContext.config } returns EmarsysConfig(applicationCode = APPLICATION_CODE)

        mobileEngageSession.endSession()

        verifySessionEventNotRegistered(sessionEndEvent)
        sessionContext.sessionId shouldBe null
        sessionContext.sessionStart shouldBe SESSION_START
    }

    @Test
    fun testEndSession_shouldNotDoAnything_whenSessionStartIsNull() = runTest {
        sessionContext.sessionStart = null
        every { mockSdkContext.config } returns EmarsysConfig(applicationCode = APPLICATION_CODE)

        mobileEngageSession.endSession()

        verifySessionEventNotRegistered(sessionEndEvent)
        sessionContext.sessionId shouldBe SESSION_ID
        sessionContext.sessionStart shouldBe null
    }

    @Test
    fun testEndSession_shouldNotDoAnything_whenApplicationCodeIsNull() = runTest {
        every { mockSdkContext.config } returns EmarsysConfig(applicationCode = null)

        mobileEngageSession.endSession()

        verifySessionEventNotRegistered(sessionEndEvent)
        sessionContext.sessionId shouldBe SESSION_ID
        sessionContext.sessionStart shouldBe SESSION_START
    }

    @Test
    fun testEndSession_shouldNotDoAnything_whenContactTokenIsNull() = runTest {
        sessionContext.contactToken = null
        every { mockSdkContext.config } returns EmarsysConfig(applicationCode = APPLICATION_CODE)

        mobileEngageSession.endSession()

        verifySessionEventNotRegistered(sessionEndEvent)
        sessionContext.sessionId shouldBe SESSION_ID
        sessionContext.sessionStart shouldBe SESSION_START
    }

    private suspend fun verifySessionEventNotRegistered(sessionEvent: Event) {
        verifyWithSuspend {
            mockSdkContext.config
            mockSdkLogger.debug(isAny())
            repeat(0) {
                mockEventClient.registerEvent(sessionEvent)
            }
        }
    }
}