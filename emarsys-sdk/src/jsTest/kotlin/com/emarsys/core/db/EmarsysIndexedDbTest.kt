package com.emarsys.core.db

import dev.mokkery.MockMode
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.test.runTest
import web.idb.indexedDB
import kotlin.test.BeforeTest
import kotlin.test.Test

class EmarsysIndexedDbTest {

    private lateinit var emarsysIndexedDb: EmarsysIndexedDb

    @BeforeTest
    fun setup() = runTest {
        emarsysIndexedDb = EmarsysIndexedDb(indexedDB, sdkLogger = mock(MockMode.autofill))
    }

    @Test
    fun open_shouldOpenDatabase_andReturnDatabaseInstance() = runTest {
        val database = emarsysIndexedDb.open()

        database shouldNotBe null
        database.name shouldBe "EMARSYS_SDK_DB"
        database.version shouldBe 1.0
    }

    @Test
    fun open_shouldOpenDatabase_WithEventsObjectStore() = runTest {
        val database = emarsysIndexedDb.open()

        database shouldNotBe null
        database.objectStoreNames.contains("events") shouldBe true
    }

    @Test
    fun open_shouldReturnExistingDatabaseInstance_whenItHasAlreadyBeenOpened() = runTest {
        val database1 = emarsysIndexedDb.open()
        val database2 = emarsysIndexedDb.open()

        database1 shouldNotBe null
        database1 shouldBe database2
    }
}