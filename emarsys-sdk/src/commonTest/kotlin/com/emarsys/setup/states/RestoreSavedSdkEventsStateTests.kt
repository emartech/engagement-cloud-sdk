package com.emarsys.setup.states

import com.emarsys.core.SdkEventEmitterApi
import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.networking.clients.event.model.SdkEvent
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class RestoreSavedSdkEventsStateTests {

    private lateinit var mockEventsDao: EventsDaoApi
    private lateinit var mockSdkEventEmitter: SdkEventEmitterApi

    private lateinit var restoreSavedSdkEventsState: RestoreSavedSdkEventsState

    companion object {
        val testEvent1 = SdkEvent.External.Custom("custom", "testId1", "testName1")
        val testEvent2 = SdkEvent.External.Custom("custom", "testId2", "testName2")

    }

    @BeforeTest
    fun setup() {
        mockEventsDao = mock()
        mockSdkEventEmitter = mock(MockMode.autoUnit)
        restoreSavedSdkEventsState = RestoreSavedSdkEventsState(
            eventsDao = mockEventsDao,
            sdkEventEmitter = mockSdkEventEmitter,
            sdkLogger = mock(MockMode.autofill),
        )
    }

    @Test
    fun testActive_shouldGetEventsFromDao_andEmitThem() = runTest {
        everySuspend { mockEventsDao.getEvents() } returns flowOf(testEvent1, testEvent2)

        restoreSavedSdkEventsState.active()

        verifySuspend(VerifyMode.order) {
            mockEventsDao.getEvents()
            mockSdkEventEmitter.emitEvent(testEvent1)
            mockSdkEventEmitter.emitEvent(testEvent2)
        }
    }

    @Test
    fun testActive_shouldStillContinueEmittingEvents_whenEmitFails() = runTest {
        everySuspend { mockEventsDao.getEvents() } returns flowOf(testEvent1, testEvent2)
        everySuspend { mockSdkEventEmitter.emitEvent(testEvent1) } throws Exception("Emit failed")

        restoreSavedSdkEventsState.active()

        verifySuspend(VerifyMode.order) {
            mockEventsDao.getEvents()
            mockSdkEventEmitter.emitEvent(testEvent1)
            mockSdkEventEmitter.emitEvent(testEvent2)
        }
    }
}