package com.sap.ec.init.states

import com.sap.ec.core.channel.SdkEventEmitterApi
import com.sap.ec.core.db.events.EventsDaoApi
import com.sap.ec.core.log.LogLevel
import com.sap.ec.core.state.State
import com.sap.ec.event.SdkEvent
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)
class RestoreOperationalEventsStateTests {
    private companion object {
        const val TEST_APPLICATION_CODE = "testAppCode"
    }

    private lateinit var mockSdkEventEmitter: SdkEventEmitterApi
    private lateinit var mockEventsDao: EventsDaoApi
    private lateinit var restoreOperationalEventsState: State

    @BeforeTest
    fun setup() {
        mockEventsDao = mock()
        mockSdkEventEmitter = mock()
        everySuspend { mockSdkEventEmitter.emitEvent(any()) } returns Unit
        restoreOperationalEventsState =
            RestoreOperationalEventsState(mockSdkEventEmitter, mockEventsDao)
    }

    @Test
    fun active_shouldReEmit_operationalAndLogEvents_toEventFlow() = runTest {
        val testEvent1 = SdkEvent.Internal.Sdk.UnlinkContact()
        val testEvent2 = SdkEvent.Internal.Sdk.Log(LogLevel.Info)
        val testEvent3 = SdkEvent.Internal.Sdk.AppStart()
        val testEvent4 =
            SdkEvent.Internal.Sdk.ClearPushToken(applicationCode = TEST_APPLICATION_CODE)

        everySuspend { mockEventsDao.getEvents() } returns
                flowOf(testEvent1, testEvent2, testEvent3, testEvent4)

        restoreOperationalEventsState.active()

        advanceUntilIdle()

        verifySuspend { mockEventsDao.getEvents() }
        verifySuspend { mockSdkEventEmitter.emitEvent(testEvent2) }
        verifySuspend { mockSdkEventEmitter.emitEvent(testEvent4) }
        verifySuspend(VerifyMode.exactly(0)) { mockSdkEventEmitter.emitEvent(testEvent1) }
        verifySuspend(VerifyMode.exactly(0)) { mockSdkEventEmitter.emitEvent(testEvent3) }
    }
}