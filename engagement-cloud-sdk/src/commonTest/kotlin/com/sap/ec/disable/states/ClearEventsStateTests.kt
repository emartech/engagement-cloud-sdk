package com.sap.ec.disable.states

import com.sap.ec.core.db.events.EventsDaoApi
import com.sap.ec.core.log.LogLevel
import com.sap.ec.event.SdkEvent
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)
class ClearEventsStateTests {
    private companion object {
        const val TEST_APPLICATION_CODE = "testAppCode"
    }

    private lateinit var clearEventsState: ClearEventsState
    private lateinit var mockEventsDao: EventsDaoApi

    @BeforeTest
    fun setup() {
        mockEventsDao = mock(MockMode.autofill)
        clearEventsState = ClearEventsState(mockEventsDao)
    }

    @Test
    fun testActive_shouldClearAllEvents_fromDatabase_except_Log_and_Operational() = runTest {
        val testEvent1 = SdkEvent.Internal.Sdk.UnlinkContact()
        val testEvent2 = SdkEvent.Internal.Sdk.Log(LogLevel.Info)
        val testEvent3 = SdkEvent.Internal.Sdk.AppStart()
        val testEvent4 =
            SdkEvent.Internal.Sdk.ClearPushToken(applicationCode = TEST_APPLICATION_CODE)

        val testEvents = flowOf(testEvent1, testEvent2, testEvent3, testEvent4)
        everySuspend { mockEventsDao.getEvents() } returns testEvents

        val result = clearEventsState.active()

        advanceUntilIdle()

        verifySuspend { mockEventsDao.removeEvent(testEvent1) }
        verifySuspend { mockEventsDao.removeEvent(testEvent3) }
        verifySuspend(VerifyMode.exactly(0)) { mockEventsDao.removeEvent(testEvent2) }
        verifySuspend(VerifyMode.exactly(0)) { mockEventsDao.removeEvent(testEvent4) }
        result shouldBe Result.success(Unit)
    }

    @Test
    fun testActive_shouldReturn_failure_ifDBOperation_fails() = runTest {
        val testEvent = SdkEvent.Internal.Sdk.UnlinkContact()

        val testEvents = flowOf(testEvent)
        everySuspend { mockEventsDao.getEvents() } returns testEvents
        val testException = Exception("failure")
        everySuspend { mockEventsDao.removeEvent(testEvent) } throws testException

        val result = clearEventsState.active()

        verifySuspend { mockEventsDao.removeEvent(testEvent) }
        result shouldBe Result.failure(testException)
    }
}