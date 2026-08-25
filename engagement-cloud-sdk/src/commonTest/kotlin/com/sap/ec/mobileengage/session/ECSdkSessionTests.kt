@file:OptIn(ExperimentalTime::class)

package com.sap.ec.mobileengage.session

import com.sap.ec.TestEngagementCloudSDKConfig
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.lifecycle.LifecycleEvent
import com.sap.ec.core.log.LogEntry
import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.context.RequestContextApi
import com.sap.ec.core.providers.InstantProvider
import com.sap.ec.core.providers.UuidProviderApi
import com.sap.ec.core.session.SessionContext
import com.sap.ec.core.session.SessionId
import com.sap.ec.event.SdkEvent
import com.sap.ec.watchdog.lifecycle.LifecycleWatchDog
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.answering.sequentiallyReturns
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifyNoMoreCalls
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
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class ECSdkSessionTests {
    private companion object Companion {
        const val APPLICATION_CODE = "testApplicationCode"
        const val CONTACT_TOKEN = "testContactToken"
        const val SESSION_START_1 = 123456789L
        val SESSION_START_1_UTC = Instant.parse("1970-01-02T10:17:36.789Z")
        const val SESSION_END = 123457789L
        val SESSION_END_UTC = Instant.parse("1970-01-02T10:17:37.789Z")
        const val SESSION_START_2 = 123458789L
        val SESSION_START_2_UTC = Instant.parse("1970-01-02T10:17:38.789Z")
        const val SESSION_DURATION = 1000L
        val SESSION_ID_1 = SessionId("testSessionId1")
        val SESSION_ID_2 = SessionId("testSessionId2")
        const val SESSION_END_EVENT_ID = "sessionEndEventId"
        const val SESSION_START_EVENT_ID_1 = "sessionStartEventId1"
        const val SESSION_START_EVENT_ID_2 = "sessionStartEventId2"
        val sessionStartEvent = SdkEvent.Internal.Sdk.SessionStart(
            id = SESSION_START_EVENT_ID_1,
            timestamp = SESSION_START_1_UTC
        )
        val sessionEndEvent = SdkEvent.Internal.Sdk.SessionEnd(
            id = SESSION_END_EVENT_ID,
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
    private lateinit var ecSdkSession: ECSdkSession

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
        sessionContext = SessionContext()
        everySuspend { mockSdkContext.getSdkConfig() } returns TestEngagementCloudSDKConfig(
            applicationCode = APPLICATION_CODE
        )

        every { mockUuidProvider.provide() } sequentiallyReturns listOf(
            SESSION_START_EVENT_ID_1,
            SESSION_ID_1.value,
            SESSION_END_EVENT_ID,
            SESSION_START_EVENT_ID_2,
            SESSION_ID_2.value
        )
        everySuspend { mockSdkEventDistributor.registerEvent(any()) } returns mock()
        everySuspend { mockSdkLogger.debug(any<LogEntry>()) } returns Unit
        everySuspend { mockSdkLogger.error(message = any()) } returns Unit

        ecSdkSession = ECSdkSession(
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
        every { mockTimestampProvider.provide() } returns Instant.fromEpochMilliseconds(
            SESSION_START_1
        )
        val sharedFlow = MutableSharedFlow<LifecycleEvent>()
        ecSdkSession.subscribe(object : LifecycleWatchDog {
            override val lifecycleEvents: SharedFlow<LifecycleEvent> = sharedFlow

            override suspend fun register() {}
        })

        sharedFlow.emit(LifecycleEvent.OnForeground)
        advanceUntilIdle()

        sessionContext.sessionStart shouldBe SESSION_START_1
        sessionContext.sessionId shouldBe SESSION_ID_1
    }

    @Test
    fun testSubscribe_shouldCallEndSession() = runTest {
        every { mockTimestampProvider.provide() } returns Instant.fromEpochMilliseconds(
            SESSION_START_1
        )
        val sharedFlow = MutableSharedFlow<LifecycleEvent>()
        ecSdkSession.subscribe(object : LifecycleWatchDog {
            override val lifecycleEvents: SharedFlow<LifecycleEvent> = sharedFlow

            override suspend fun register() {}
        })

        sharedFlow.emit(LifecycleEvent.OnForeground)
        advanceUntilIdle()

        sessionContext.sessionStart shouldBe SESSION_START_1
        sessionContext.sessionId shouldBe SESSION_ID_1

        every { mockTimestampProvider.provide() } returns Instant.fromEpochMilliseconds(SESSION_END)

        sharedFlow.emit(LifecycleEvent.OnBackground)
        advanceUntilIdle()

        sessionContext.sessionStart shouldBe null
        sessionContext.sessionId shouldBe null
    }


    @Test
    fun testStartSession_shouldTrackSessionStartEvent() = runTest {
        every { mockTimestampProvider.provide() } returns Instant.fromEpochMilliseconds(
            SESSION_START_1
        )

        ecSdkSession.startSession()

        verifySuspend { mockSdkEventDistributor.registerEvent(sessionStartEvent) }
    }

    @Test
    fun testEndSession_shouldTrackSessionEndEvent() = runTest {
        sessionContext.sessionId = SESSION_ID_1
        sessionContext.sessionStart = SESSION_START_1
        every { mockUuidProvider.provide() } returns SESSION_END_EVENT_ID
        every { mockTimestampProvider.provide() } returns Instant.fromEpochMilliseconds(SESSION_END)

        ecSdkSession.endSession()

        verifySuspend { mockSdkEventDistributor.registerEvent(sessionEndEvent) }
    }

    @Test
    fun testStartSession_shouldSetSession_evenWhenRegisteringEventFails() = runTest {
        everySuspend { mockSdkEventDistributor.registerEvent(sessionStartEvent) } throws RuntimeException(
            "uuid provider failed"
        )
        every { mockTimestampProvider.provide() } returns Instant.fromEpochMilliseconds(
            SESSION_START_1
        )

        ecSdkSession.startSession()

        sessionContext.sessionStart shouldBe SESSION_START_1
        sessionContext.sessionId shouldBe SESSION_ID_1
    }

    @Test
    fun testStartSession_shouldNotDoAnything_whenConfigIsNull() = runTest {
        everySuspend { mockSdkContext.getSdkConfig() } returns null

        ecSdkSession.startSession()

        verifySessionEventNotRegistered(sessionStartEvent)
        sessionContext.sessionId shouldBe null
        sessionContext.sessionStart shouldBe null
    }

    @Test
    fun testStartSession_shouldNotDoAnything_whenContactTokenIsNull() = runTest {
        every { mockRequestContext.contactToken } returns null

        ecSdkSession.startSession()

        verifySessionEventNotRegistered(sessionStartEvent)
        sessionContext.sessionId shouldBe null
        sessionContext.sessionStart shouldBe null
    }

    @Test
    fun testStartSession_shouldNotDoAnything_whenSessionIdIsNotNull() = runTest {
        sessionContext.sessionId = SESSION_ID_1

        ecSdkSession.startSession()

        verifySessionEventNotRegistered(sessionStartEvent)
        sessionContext.sessionId shouldBe SESSION_ID_1
        sessionContext.sessionStart shouldBe null
    }

    @Test
    fun testStartSession_shouldNotDoAnything_whenSessionStartIsNotNull() = runTest {
        sessionContext.sessionStart = SESSION_START_1

        ecSdkSession.startSession()

        verifySessionEventNotRegistered(sessionStartEvent)
        sessionContext.sessionId shouldBe null
        sessionContext.sessionStart shouldBe SESSION_START_1
    }

    @Test
    fun testEndSession_shouldResetSession_evenWhenRegisteringEventFails() = runTest {
        sessionContext.sessionId = SESSION_ID_1
        sessionContext.sessionStart = SESSION_START_1
        every { mockTimestampProvider.provide() } returns Instant.fromEpochMilliseconds(SESSION_END)
        everySuspend { mockSdkEventDistributor.registerEvent(sessionEndEvent) } throws RuntimeException(
            "request failed"
        )

        ecSdkSession.endSession()

        sessionContext.sessionStart shouldBe null
        sessionContext.sessionId shouldBe null
    }

    @Test
    fun testEndSession_shouldNotDoAnything_whenSessionIdIsNull() = runTest {
        sessionContext.sessionId = null
        sessionContext.sessionStart = SESSION_START_1

        ecSdkSession.endSession()

        verifySessionEventNotRegistered(sessionEndEvent)
        sessionContext.sessionId shouldBe null
        sessionContext.sessionStart shouldBe SESSION_START_1
    }

    @Test
    fun testEndSession_shouldNotDoAnything_whenSessionStartIsNull() = runTest {
        sessionContext.sessionId = SESSION_ID_1
        sessionContext.sessionStart = null

        ecSdkSession.endSession()

        verifySessionEventNotRegistered(sessionEndEvent)
        sessionContext.sessionId shouldBe SESSION_ID_1
        sessionContext.sessionStart shouldBe null
    }

    @Test
    fun testEndSession_shouldNotDoAnything_whenContactTokenIsNull() = runTest {
        sessionContext.sessionId = SESSION_ID_1
        sessionContext.sessionStart = SESSION_START_1
        every { mockRequestContext.contactToken } returns null

        ecSdkSession.endSession()

        verifySessionEventNotRegistered(sessionEndEvent)
        sessionContext.sessionId shouldBe SESSION_ID_1
        sessionContext.sessionStart shouldBe SESSION_START_1
    }

    @Test
    fun testRestartSession_shouldEndCurrentSessionAndStartNew_whenSessionIsActive() = runTest {
        sessionContext.sessionId = SESSION_ID_1
        sessionContext.sessionStart = SESSION_START_1
        every { mockUuidProvider.provide() } sequentiallyReturns listOf(
            SESSION_END_EVENT_ID,
            SESSION_START_EVENT_ID_2,
            SESSION_ID_2.value,
        )
        val restartedSessionStartEvent = SdkEvent.Internal.Sdk.SessionStart(
            id = SESSION_START_EVENT_ID_2,
            timestamp = SESSION_START_2_UTC
        )
        every { mockTimestampProvider.provide() } sequentiallyReturns listOf(
            Instant.fromEpochMilliseconds(
                SESSION_END
            ), Instant.fromEpochMilliseconds(SESSION_START_2)
        )

        ecSdkSession.restartSession()

        verifySuspend {
            mockSdkEventDistributor.registerEvent(sessionEndEvent)
            mockSdkEventDistributor.registerEvent(restartedSessionStartEvent)
        }
        sessionContext.sessionStart shouldBe SESSION_START_2
        sessionContext.sessionId shouldBe SESSION_ID_2
    }

    @Test
    fun testRestartSession_shouldStartSession_whenNoSessionIsActive() = runTest {
        sessionContext.sessionId = null
        sessionContext.sessionStart = null
        every { mockTimestampProvider.provide() } returns Instant.fromEpochMilliseconds(
            SESSION_START_1
        )

        ecSdkSession.restartSession()

        verifySessionEventNotRegistered(sessionEndEvent)
        verifySuspend { mockSdkEventDistributor.registerEvent(sessionStartEvent) }
        verifyNoMoreCalls(mockSdkEventDistributor)
        sessionContext.sessionStart shouldBe SESSION_START_1
        sessionContext.sessionId shouldBe SESSION_ID_1
    }

    private fun verifySessionEventNotRegistered(sessionEvent: SdkEvent) {
        verifySuspend {
            mockSdkContext.getSdkConfig()
            mockSdkLogger.debug(any<LogEntry>())
            repeat(0) {
                mockSdkEventDistributor.registerEvent(sessionEvent)
            }
        }
    }
}