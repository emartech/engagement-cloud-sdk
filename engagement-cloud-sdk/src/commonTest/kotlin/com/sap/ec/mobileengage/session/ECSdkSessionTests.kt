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
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class ECSdkSessionTests {
    private companion object Companion {
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
    private lateinit var ECSdkSession: ECSdkSession

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

        ECSdkSession = ECSdkSession(
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
        every { mockSdkContext.config } returns TestEngagementCloudSDKConfig(applicationCode = APPLICATION_CODE)
        every { mockTimestampProvider.provide() } returns Instant.fromEpochMilliseconds(
            SESSION_START
        )
        val sharedFlow = MutableSharedFlow<LifecycleEvent>()
        ECSdkSession.subscribe(object : LifecycleWatchDog {
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
        every { mockSdkContext.config } returns TestEngagementCloudSDKConfig(applicationCode = APPLICATION_CODE)
        every { mockTimestampProvider.provide() } returns Instant.fromEpochMilliseconds(
            SESSION_START
        )
        val sharedFlow = MutableSharedFlow<LifecycleEvent>()
        ECSdkSession.subscribe(object : LifecycleWatchDog {
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
        every { mockSdkContext.config } returns TestEngagementCloudSDKConfig(applicationCode = APPLICATION_CODE)
        every { mockTimestampProvider.provide() } returns Instant.fromEpochMilliseconds(
            SESSION_START
        )

        ECSdkSession.startSession()

        verifySuspend { mockSdkEventDistributor.registerEvent(sessionStartEvent) }
    }

    @Test
    fun testEndSession_shouldTrackSessionEndEvent() = runTest {
        every { mockTimestampProvider.provide() } returns Instant.fromEpochMilliseconds(SESSION_END)
        every { mockSdkContext.config } returns TestEngagementCloudSDKConfig(applicationCode = APPLICATION_CODE)

        ECSdkSession.endSession()

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
        every { mockSdkContext.config } returns TestEngagementCloudSDKConfig(applicationCode = APPLICATION_CODE)

        ECSdkSession.startSession()

        sessionContext.sessionStart shouldBe SESSION_START
        sessionContext.sessionId shouldBe SESSION_ID
    }

    @Test
    fun testStartSession_shouldNotDoAnything_whenConfigIsNull() = runTest {
        sessionContext.sessionId = null
        sessionContext.sessionStart = null
        every { mockSdkContext.config } returns null

        ECSdkSession.startSession()

        verifySessionEventNotRegistered(sessionStartEvent)
        sessionContext.sessionId shouldBe null
        sessionContext.sessionStart shouldBe null
    }

    @Test
    fun testStartSession_shouldNotDoAnything_whenContactTokenIsNull() = runTest {
        every { mockRequestContext.contactToken } returns null
        every { mockSdkContext.config } returns TestEngagementCloudSDKConfig(applicationCode = APPLICATION_CODE)

        ECSdkSession.startSession()

        verifySessionEventNotRegistered(sessionStartEvent)
        sessionContext.sessionId shouldBe SESSION_ID
        sessionContext.sessionStart shouldBe SESSION_START
    }

    @Test
    fun testStartSession_shouldNotDoAnything_whenSessionIdIsNull() = runTest {
        sessionContext.sessionId = null
        every { mockSdkContext.config } returns TestEngagementCloudSDKConfig(applicationCode = APPLICATION_CODE)

        ECSdkSession.startSession()

        verifySessionEventNotRegistered(sessionStartEvent)
        sessionContext.sessionId shouldBe null
        sessionContext.sessionStart shouldBe SESSION_START
    }

    @Test
    fun testStartSession_shouldNotDoAnything_whenSessionStartIsNull() = runTest {
        sessionContext.sessionStart = null
        every { mockSdkContext.config } returns TestEngagementCloudSDKConfig(applicationCode = APPLICATION_CODE)

        ECSdkSession.startSession()

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

        every { mockSdkContext.config } returns TestEngagementCloudSDKConfig(applicationCode = APPLICATION_CODE)

        ECSdkSession.endSession()

        sessionContext.sessionStart shouldBe null
        sessionContext.sessionId shouldBe null
    }

    @Test
    fun testEndSession_shouldNotDoAnything_whenSessionIdIsNull() = runTest {
        sessionContext.sessionId = null
        every { mockSdkContext.config } returns TestEngagementCloudSDKConfig(applicationCode = APPLICATION_CODE)

        ECSdkSession.endSession()

        verifySessionEventNotRegistered(sessionEndEvent)
        sessionContext.sessionId shouldBe null
        sessionContext.sessionStart shouldBe SESSION_START
    }

    @Test
    fun testEndSession_shouldNotDoAnything_whenSessionStartIsNull() = runTest {
        sessionContext.sessionStart = null
        every { mockSdkContext.config } returns TestEngagementCloudSDKConfig(applicationCode = APPLICATION_CODE)

        ECSdkSession.endSession()

        verifySessionEventNotRegistered(sessionEndEvent)
        sessionContext.sessionId shouldBe SESSION_ID
        sessionContext.sessionStart shouldBe null
    }

    @Test
    fun testEndSession_shouldNotDoAnything_whenContactTokenIsNull() = runTest {
        every { mockRequestContext.contactToken } returns null
        every { mockSdkContext.config } returns TestEngagementCloudSDKConfig(applicationCode = APPLICATION_CODE)

        ECSdkSession.endSession()

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