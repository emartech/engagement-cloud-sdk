package com.sap.ec.core.db

import dev.mokkery.MockMode
import dev.mokkery.mock
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.test.runTest
import web.idb.IDBDatabase
import web.idb.indexedDB
import kotlin.test.BeforeTest
import kotlin.test.Test

class EngagementCloudIndexedDbTest {

    private lateinit var engagementCloudIndexedDb: EngagementCloudIndexedDb

    @BeforeTest
    fun setup() = runTest {
        engagementCloudIndexedDb = EngagementCloudIndexedDb(indexedDB, sdkLogger = mock(MockMode.autofill))
    }

    @Test
    fun execute_shouldOpenDatabase_andReturnDatabaseInstance() = runTest {
        val database = engagementCloudIndexedDb.execute { database -> database }

        database shouldNotBe null
        database.name shouldBe "SAP_ENGAGEMENT_CLOUD_SDK_DB"
        database.version shouldBe 1.0
    }

    @Test
    fun execute_shouldOpenDatabase_WithEventsObjectStores() = runTest {
        val database = engagementCloudIndexedDb.execute { database -> database }

        database shouldNotBe null
        database.objectStoreNames.contains("events") shouldBe true
        database.objectStoreNames.contains("clientId") shouldBe true
    }

    @Test
    fun execute_shouldOpenDatabase_callCallback_andCloseDatabase() = runTest {
        val testResult = "testResult"
        var database: IDBDatabase? = null

        val result = engagementCloudIndexedDb.execute { db ->
            database = db
            testResult
        }

        result shouldBe testResult
        assertDatabaseIsClosed(database)
    }

    @Test
    fun execute_shouldOpenDatabase_AndCloseIt_evenIfExceptionIsThrownDuringExecution() = runTest {
        val testExceptionMessage = "testMessage"
        var database: IDBDatabase? = null
        val exception = shouldThrow<Exception> {
            engagementCloudIndexedDb.execute { db ->
                database = db
                throw Exception(testExceptionMessage)
            }
        }

        exception.message shouldBe testExceptionMessage
        assertDatabaseIsClosed(database)
    }

    private fun assertDatabaseIsClosed(database: IDBDatabase?) {
        val throwable = try {
            database!!.transaction("events")
        } catch (throwable: Throwable) {
            throwable
        }

        throwable.unsafeCast<Error>().message shouldBe "Failed to execute 'transaction' on 'IDBDatabase': The database connection is closing."
    }
}