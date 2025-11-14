package com.emarsys.core.db.events

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.emarsys.event.OnlineSdkEvent
import com.emarsys.event.SdkEvent
import com.emarsys.sqldelight.EmarsysDB
import com.emarsys.util.JsonUtil
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class IosSqDelightEventsDaoTests {
    private companion object {
        const val DB_NAME = "test.db"
        val TIMESTAMP = Clock.System.now()
        val ATTRIBUTES = buildJsonObject {
            put("key", "value")
        }
    }

    private lateinit var eventsDao: EventsDaoApi
    private lateinit var db: EmarsysDB

    @BeforeTest
    fun setup() {
        val driver = NativeSqliteDriver(EmarsysDB.Schema, DB_NAME)
        db = EmarsysDB(driver)
        eventsDao = IosSqDelightEventsDao(db, JsonUtil.json)
    }

    @AfterTest
    fun tearDown() {
        db.eventsQueries.deleteAll()
    }

    @Test
    fun testInsertEvent() = runTest {
        val event = SdkEvent.External.Custom(
            type = "custom",
            id = "testId",
            name = "test",
            attributes = ATTRIBUTES,
            timestamp = TIMESTAMP
        )

        eventsDao.insertEvent(event)

        val result = eventsDao.getEvents().firstOrNull()

        advanceUntilIdle()

        result shouldBe event
    }

    @Test
    fun testUpsertEvent() = runTest {
        val event = SdkEvent.External.Custom(
            type = "custom",
            id = "testId",
            name = "test",
            attributes = ATTRIBUTES,
            timestamp = TIMESTAMP,
            nackCount = 3
        )

        eventsDao.insertEvent(event)

        eventsDao.upsertEvent(event.copy(nackCount = 4))

        val result = eventsDao.getEvents().firstOrNull()

        advanceUntilIdle()

        (result as OnlineSdkEvent).nackCount shouldBe 4
    }

    @Test
    fun testGetEvents_whenNothingWasInserted() = runTest {
        val result = eventsDao.getEvents().firstOrNull()

        advanceUntilIdle()

        result shouldBe null
    }

    @Test
    fun testRemoveEvent_shouldDoNothing_whenNothingWasInserted() = runTest {
        eventsDao.removeEvent(
            SdkEvent.External.Custom(
                "custom",
                "testId",
                "test",
                ATTRIBUTES,
                TIMESTAMP
            )
        )
    }

    @Test
    fun testRemoveEvent_shouldRemoveEvent_ById() = runTest {
        val testEvent = SdkEvent.External.Custom("custom", "testId", "test", ATTRIBUTES, TIMESTAMP)
        eventsDao.insertEvent(testEvent)

        eventsDao.removeEvent(testEvent)

        eventsDao.getEvents().toList() shouldBe emptyList()
    }

    @Test
    fun testRemoveAll_shouldRemoveAllEvents() = runTest {
        val testEvent1 =
            SdkEvent.External.Custom("custom1", "testId", "test", ATTRIBUTES, TIMESTAMP)
        val testEvent2 =
            SdkEvent.External.Custom("custom2", "testId", "test", ATTRIBUTES, TIMESTAMP)
        eventsDao.insertEvent(testEvent1)
        eventsDao.insertEvent(testEvent2)

        eventsDao.removeAll()

        eventsDao.getEvents().toList() shouldBe emptyList()
    }
}