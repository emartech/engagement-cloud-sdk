package com.sap.ec.core.db.events

import com.sap.ec.core.db.ECIndexedDbObjectStoreApi
import com.sap.ec.event.SdkEvent
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class JSEventsDaoTest {
    private lateinit var jsEventsDao: JSEventsDao
    private lateinit var mockECIndexedDbObjectStore: ECIndexedDbObjectStoreApi<SdkEvent>

    @BeforeTest
    fun setup() {
        mockECIndexedDbObjectStore = mock(mode = MockMode.autofill)
        jsEventsDao = JSEventsDao(mockECIndexedDbObjectStore, logger = mock(MockMode.autofill))
    }

    @Test
    fun insertEvent_shouldCallPut_onObjectStore_withCorrectParams() = runTest {
        val testEvent = SdkEvent.External.Custom("testId", "testName")

        jsEventsDao.insertEvent(testEvent)

        verifySuspend { mockECIndexedDbObjectStore.put(testEvent.id, testEvent) }
    }

    @Test
    fun upsertEvent_shouldCallPut_onObjectStore_withCorrectParams() = runTest {
        val testEvent = SdkEvent.External.Custom("testId", "testName")

        jsEventsDao.insertEvent(testEvent)

        verifySuspend { mockECIndexedDbObjectStore.put(testEvent.id, testEvent) }
    }

    @Test
    fun getEvents_shouldReturn_flowOfStoredEvents() = runTest {
        val testEvent = SdkEvent.External.Custom("testId", "testName")
        val testEvent2 = SdkEvent.External.Custom("testId2", "testName2")
        val expectedEvents = flowOf(testEvent, testEvent2)

        everySuspend { mockECIndexedDbObjectStore.getAll() } returns expectedEvents

        jsEventsDao.getEvents() shouldBe expectedEvents
    }

    @Test
    fun removeAll_shouldDelegateToObjectStore() = runTest {
        jsEventsDao.removeAll()

        verifySuspend { mockECIndexedDbObjectStore.removeAll() }
    }
}