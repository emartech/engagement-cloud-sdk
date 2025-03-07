package com.emarsys.core.db.events

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.emarsys.networking.clients.event.model.SdkEvent
import com.emarsys.sqldelight.EmarsysDB
import com.emarsys.util.JsonUtil
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
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
    fun testGetEvents_whenNothingWasInserted() = runTest {
        val result = eventsDao.getEvents().firstOrNull()

        advanceUntilIdle()

        result shouldBe null
    }
}