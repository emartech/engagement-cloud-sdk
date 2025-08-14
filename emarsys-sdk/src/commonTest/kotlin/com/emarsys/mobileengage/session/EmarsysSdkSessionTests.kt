@file:OptIn(ExperimentalTime::class)

package com.emarsys.mobileengage.session

import com.emarsys.TestEmarsysConfig
import com.emarsys.context.SdkContextApi
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.lifecycle.LifecycleEvent
import com.emarsys.core.log.LogEntry
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.context.RequestContextApi
import com.emarsys.core.providers.InstantProvider
import com.emarsys.core.providers.UuidProviderApi
import com.emarsys.core.session.SessionContext
import com.emarsys.core.session.SessionId
import com.emarsys.event.SdkEvent
import com.emarsys.watchdog.lifecycle.LifecycleWatchDog
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
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
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class EmarsysSdkSessionTests {
    private companion object {
        const val APPLICATION_CODE = "testApplicationCode"
        const val CONTACT_TOKEN = "testContactToken"
        val SESSION_START_UTC = Instant.parse("1970-01-02T10:17:36.789Z")
        const val SESSION_START = 123456789L
        const val SESSION_END = 123457789L
        val SESSION_END_UTC = Instant.parse("1970-01-02T10:17:37.789Z")
        const val SESSION_DURATION = 1000L
        val SESSION_ID = SessionId("testSessionId")
        val sessionStartEvent = SdkEvent.Internal.Sdk.SessionStart(
            id = SESSION_ID.value,
            timestamp = SESSION_START_UTC
        )
        val sessionEndEvent = SdkEvent.Internal.Sdk.SessionEnd(
            id = SESSION_ID.value,
            duration = SESSION_DURATION,
            timestamp = SESSION_END_UTC
        )
    }

    private lateinit var mockTimestampProvider: InstantProvider
    private lateinit var mockUuidProvider: UuidProviderApi
    private lateinit var mockSdkLogger: Logger
    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var mockRequestContext: RequestContextApi
    private lateinit var sessionContext: SessionContext
    private lateinit var sdkDispatcher: CoroutineDispatcher
    private lateinit var emarsysSdkSession: EmarsysSdkSession

    init {
        Dispatchers.setMain(
            StandardTestDispatcher()
        )
    }

    @BeforeTest
    fun setUp() = runTest {
        mockTimestampProvider = mock()
        mockUuidProvider = mock()
        mockSdkLogger = mock(MockMode.autofill)
        mockSdkEventDistributor = mock()
        mockSdkContext = mock()
        sdkDispatcher = StandardTestDispatcher()
        mockRequestContext = mock(MockMode.autofill)
        every { mockRequestContext.contactToken } returns CONTACT_TOKEN
        sessionContext = SessionContext(
            SESSION_ID,
            SESSION_START
        )

        every { mockUuidProvider.provide() } returns SESSION_ID.value
        everySuspend { mockSdkEventDistributor.registerEvent(sessionStartEvent) } returns mock()
        everySuspend { mockSdkEventDistributor.registerEvent(sessionEndEvent) } returns mock()
        everySuspend { mockSdkLogger.debug(any<LogEntry>()) } returns Unit
        everySuspend { mockSdkLogger.debug(any<LogEntry>()) } returns Unit
        everySuspend { mockSdkLogger.error(message = any()) } returns Unit

        emarsysSdkSession = EmarsysSdkSession(
            mockTimestampProvider,
            mockUuidProvider,
            mockRequestContext,
            sessionContext,
            mockSdkContext,
            mockSdkEventDistributor,
            sdkDispatcher,
            mockSdkLogger
        )
    }

    @Test
    fun testSubscribe_shouldCallStartSession() = runTest {
        sessionContext.sessionId = null
        sessionContext.sessionStart = null
        every { mockSdkContext.config } returns TestEmarsysConfig(applicationCode = APPLICATION_CODE)
        every { mockTimestampProvider.provide() } returns Instant.fromEpochMilliseconds(
            SESSION_START
        )
        val sharedFlow = MutableSharedFlow<LifecycleEvent>()
        emarsysSdkSession.subscribe(object : LifecycleWatchDog {
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
        everySuspend { mockSdkEventDistributor.registerEvent(sessionStartEvent) } returns mock(
            MockMode.autofill
        )
        everySuspend { mockSdkEventDistributor.registerEvent(sessionEndEvent) } returns mock(
            MockMode.autofill
        )
        every { mockSdkContext.config } returns TestEmarsysConfig(applicationCode = APPLICATION_CODE)
        every { mockTimestampProvider.provide() } returns Instant.fromEpochMilliseconds(
            SESSION_START
        )
        val sharedFlow = MutableSharedFlow<LifecycleEvent>()
        emarsysSdkSession.subscribe(object : LifecycleWatchDog {
            override val lifecycleEvents: SharedFlow<LifecycleEvent> = sharedFlow

            override suspend fun register() {}
        })

        sharedFlow.emit(LifecycleEvent.OnForeground)
        advanceUntilIdle()

        sessionContext.sessionStart shouldBe SESSION_START
        sessionContext.sessionId shouldBe SESSION_ID

        every { mockTimestampProvider.provide() } returns Instant.fromEpochMilliseconds(SESSION_END)

        sharedFlow.emit(LifecycleEvent.OnBackground)
        advanceUntilIdle()

        sessionContext.sessionStart shouldBe null
        sessionContext.sessionId shouldBe null
    }


    @Test
    fun testStartSession_shouldTrackSessionStartEvent() = runTest {
        sessionContext.sessionId = null
        sessionContext.sessionStart = null
        every { mockSdkContext.config } returns TestEmarsysConfig(applicationCode = APPLICATION_CODE)
        every { mockTimestampProvider.provide() } returns Instant.fromEpochMilliseconds(
            SESSION_START
        )

        emarsysSdkSession.startSession()

        verifySuspend { mockSdkEventDistributor.registerEvent(sessionStartEvent) }
    }

    @Test
    fun testEndSession_shouldTrackSessionEndEvent() = runTest {
        every { mockTimestampProvider.provide() } returns Instant.fromEpochMilliseconds(SESSION_END)
        every { mockSdkContext.config } returns TestEmarsysConfig(applicationCode = APPLICATION_CODE)

        emarsysSdkSession.endSession()

        verifySuspend { mockSdkEventDistributor.registerEvent(sessionEndEvent) }
    }

    @Test
    fun testStartSession_shouldSetSession_evenWhenRegisteringEventFails() = runTest {
        sessionContext.sessionId = null
        sessionContext.sessionStart = null
        everySuspend { mockSdkEventDistributor.registerEvent(sessionStartEvent) } throws RuntimeException(
            "uuid provider failed"
        )
        every { mockTimestampProvider.provide() } returns Instant.fromEpochMilliseconds(
            SESSION_START
        )
        every { mockSdkContext.config } returns TestEmarsysConfig(applicationCode = APPLICATION_CODE)

        emarsysSdkSession.startSession()

        sessionContext.sessionStart shouldBe SESSION_START
        sessionContext.sessionId shouldBe SESSION_ID
    }

    @Test
    fun testStartSession_shouldNotDoAnything_whenApplicationCodeIsNull() = runTest {
        sessionContext.sessionId = null
        sessionContext.sessionStart = null
        every { mockSdkContext.config } returns TestEmarsysConfig()

        emarsysSdkSession.startSession()

        verifySessionEventNotRegistered(sessionStartEvent)
        sessionContext.sessionId shouldBe null
        sessionContext.sessionStart shouldBe null
    }

    @Test
    fun testStartSession_shouldNotDoAnything_whenContactTokenIsNull() = runTest {
        every { mockRequestContext.contactToken } returns null
        every { mockSdkContext.config } returns TestEmarsysConfig(applicationCode = APPLICATION_CODE)

        emarsysSdkSession.startSession()

        verifySessionEventNotRegistered(sessionStartEvent)
        sessionContext.sessionId shouldBe SESSION_ID
        sessionContext.sessionStart shouldBe SESSION_START
    }

    @Test
    fun testStartSession_shouldNotDoAnything_whenSessionIdIsNull() = runTest {
        sessionContext.sessionId = null
        every { mockSdkContext.config } returns TestEmarsysConfig(applicationCode = APPLICATION_CODE)

        emarsysSdkSession.startSession()

        verifySessionEventNotRegistered(sessionStartEvent)
        sessionContext.sessionId shouldBe null
        sessionContext.sessionStart shouldBe SESSION_START
    }

    @Test
    fun testStartSession_shouldNotDoAnything_whenSessionStartIsNull() = runTest {
        sessionContext.sessionStart = null
        every { mockSdkContext.config } returns TestEmarsysConfig(applicationCode = APPLICATION_CODE)

        emarsysSdkSession.startSession()

        verifySessionEventNotRegistered(sessionStartEvent)
        sessionContext.sessionId shouldBe SESSION_ID
        sessionContext.sessionStart shouldBe null
    }

    @Test
    fun testEndSession_shouldResetSession_evenWhenRegisteringEventFails() = runTest {
        every { mockTimestampProvider.provide() } returns Instant.fromEpochMilliseconds(SESSION_END)

        everySuspend { mockSdkEventDistributor.registerEvent(sessionEndEvent) } throws RuntimeException(
            "request failed"
        )

        every { mockSdkContext.config } returns TestEmarsysConfig(applicationCode = APPLICATION_CODE)

        emarsysSdkSession.endSession()

        sessionContext.sessionStart shouldBe null
        sessionContext.sessionId shouldBe null
    }

    @Test
    fun testEndSession_shouldNotDoAnything_whenSessionIdIsNull() = runTest {
        sessionContext.sessionId = null
        every { mockSdkContext.config } returns TestEmarsysConfig(applicationCode = APPLICATION_CODE)

        emarsysSdkSession.endSession()

        verifySessionEventNotRegistered(sessionEndEvent)
        sessionContext.sessionId shouldBe null
        sessionContext.sessionStart shouldBe SESSION_START
    }

    @Test
    fun testEndSession_shouldNotDoAnything_whenSessionStartIsNull() = runTest {
        sessionContext.sessionStart = null
        every { mockSdkContext.config } returns TestEmarsysConfig(applicationCode = APPLICATION_CODE)

        emarsysSdkSession.endSession()

        verifySessionEventNotRegistered(sessionEndEvent)
        sessionContext.sessionId shouldBe SESSION_ID
        sessionContext.sessionStart shouldBe null
    }

    @Test
    fun testEndSession_shouldNotDoAnything_whenApplicationCodeIsNull() = runTest {
        every { mockSdkContext.config } returns TestEmarsysConfig()

        emarsysSdkSession.endSession()

        verifySessionEventNotRegistered(sessionEndEvent)
        sessionContext.sessionId shouldBe SESSION_ID
        sessionContext.sessionStart shouldBe SESSION_START
    }

    @Test
    fun testEndSession_shouldNotDoAnything_whenContactTokenIsNull() = runTest {
        every { mockRequestContext.contactToken } returns null
        every { mockSdkContext.config } returns TestEmarsysConfig(applicationCode = APPLICATION_CODE)

        emarsysSdkSession.endSession()

        verifySessionEventNotRegistered(sessionEndEvent)
        sessionContext.sessionId shouldBe SESSION_ID
        sessionContext.sessionStart shouldBe SESSION_START
    }

    private fun verifySessionEventNotRegistered(sessionEvent: SdkEvent) {
        verifySuspend {
            mockSdkContext.config
            mockSdkLogger.debug(any<LogEntry>())
            repeat(0) {
                mockSdkEventDistributor.registerEvent(sessionEvent)
            }
        }
    }
}