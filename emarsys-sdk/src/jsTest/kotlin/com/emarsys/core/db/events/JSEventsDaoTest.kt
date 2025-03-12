package com.emarsys.core.db.events

import com.emarsys.core.db.EmarsysIndexedDbObjectStoreApi
import com.emarsys.networking.clients.event.model.SdkEvent
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

class JSEventsDaoTest {
    private lateinit var jsEventsDao: JSEventsDao
    private lateinit var mockEmarsysIndexedDbObjectStore: EmarsysIndexedDbObjectStoreApi<SdkEvent>

    @BeforeTest
    fun setup() {
        mockEmarsysIndexedDbObjectStore = mock(mode = MockMode.autofill)
        jsEventsDao = JSEventsDao(mockEmarsysIndexedDbObjectStore, logger = mock(MockMode.autofill))
    }

    @Test
    fun insertEvent_shouldCallPut_onObjectStore_withCorrectParams() = runTest {
        val testEvent = SdkEvent.External.Custom("testId", "testName")

        jsEventsDao.insertEvent(testEvent)

        verifySuspend { mockEmarsysIndexedDbObjectStore.put(testEvent.id, testEvent) }
    }

    @Test
    fun getEvents_shouldReturn_flowOfStoredEvents() = runTest {
        val testEvent = SdkEvent.External.Custom("testId", "testName")
        val testEvent2 = SdkEvent.External.Custom("testId2", "testName2")
        val expectedEvents = flowOf(testEvent, testEvent2)

        everySuspend { mockEmarsysIndexedDbObjectStore.getAll() } returns expectedEvents

        jsEventsDao.getEvents() shouldBe expectedEvents
    }
}