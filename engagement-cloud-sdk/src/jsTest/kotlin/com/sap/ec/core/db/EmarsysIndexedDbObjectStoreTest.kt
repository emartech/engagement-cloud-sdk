package com.sap.ec.core.db

import com.sap.ec.core.log.Logger
import com.sap.ec.event.SdkEvent
import com.sap.ec.util.JsonUtil
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.mock
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import js.array.asList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.StringFormat
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import web.events.EventHandler
import web.idb.IDBTransactionMode
import web.idb.IDBValidKey
import web.idb.indexedDB
import web.idb.readonly
import web.idb.readwrite
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class, ExperimentalWasmJsInterop::class)
class EmarsysIndexedDbObjectStoreTest {

    private lateinit var engagementCloudIndexedDb: EngagementCloudIndexedDb
    private lateinit var json: StringFormat
    private lateinit var mockLogger: Logger

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        json = JsonUtil.json
        mockLogger = mock(MockMode.autofill)
        engagementCloudIndexedDb = EngagementCloudIndexedDb(indexedDB, sdkLogger = mock(MockMode.autofill))
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun put_shouldStoreDataClass_successfully_andReturnId() = runTest {
        clearDatabase()
        val objectStoreConfig = EngagementCloudObjectStoreConfig.Events
        val ECIndexedDbObjectStore = ECIndexedDbObjectStore(
            engagementCloudIndexedDb,
            objectStoreConfig,
            json,
            mockLogger,
            StandardTestDispatcher()
        )

        val event = SdkEvent.External.Custom(
            "id",
            "name",
            buildJsonObject { put("key", JsonPrimitive("value")) })

        val key = ECIndexedDbObjectStore.put("id", event)

        val value = ECIndexedDbObjectStore.get("id")

        value shouldBe event
        key shouldBe "id"
        checkDb(event, objectStoreConfig)
    }

    @Test
    fun put_shouldStoreClientId_successfully_andReturnId() = runTest {
        clearDatabase()
        val objectStoreConfig = EngagementCloudObjectStoreConfig.ClientId
        val ECIndexedDbObjectStore = ECIndexedDbObjectStore(
            engagementCloudIndexedDb,
            objectStoreConfig,
            json,
            mockLogger,
            StandardTestDispatcher()
        )
        val testClientId = "testClientId"

        val key = ECIndexedDbObjectStore.put("id", testClientId)

        key shouldBe "id"
        checkDb(testClientId, objectStoreConfig)
    }

    @Test
    fun put_shouldThrowException_ifErrorHappens() = runTest {
        clearDatabase()
        val testClientId = "testClientId"
        val mockJson: StringFormat = mock(MockMode.autofill)
        val objectStoreConfig = EngagementCloudObjectStoreConfig.ClientId
        every {
            mockJson.encodeToString(
                objectStoreConfig.serializer,
                testClientId
            )
        } throws IllegalArgumentException("test exception")

        val ECIndexedDbObjectStore = ECIndexedDbObjectStore(
            engagementCloudIndexedDb,
            objectStoreConfig,
            mockJson,
            mockLogger,
            StandardTestDispatcher()
        )

        shouldThrow<IllegalArgumentException> {
            ECIndexedDbObjectStore.put(
                "id",
                testClientId
            )
        }

    }

    @Test
    fun get_shouldThrowException_ifErrorHappens() = runTest {
        clearDatabase()
        val testClientId = "testClientId"
        val objectStoreConfig = EngagementCloudObjectStoreConfig.ClientId
        val mockJson: StringFormat = mock(MockMode.autofill)
        every {
            mockJson.encodeToString(
                objectStoreConfig.serializer,
                testClientId
            )
        } returns "\"testClientId\""
        every {
            mockJson.decodeFromString(
                objectStoreConfig.serializer,
                "\"testClientId\""
            )
        } throws RuntimeException("test exception")

        val ECIndexedDbObjectStore = ECIndexedDbObjectStore(
            engagementCloudIndexedDb,
            objectStoreConfig,
            mockJson,
            mockLogger,
            StandardTestDispatcher()
        )
        ECIndexedDbObjectStore.put("id", testClientId)

        shouldThrow<RuntimeException> {
            ECIndexedDbObjectStore.get("id")
        }
    }

    @Test
    fun getAll_shouldReturnAllData() = runTest {
        clearDatabase()
        val objectStoreConfig = EngagementCloudObjectStoreConfig.Events
        val ECIndexedDbObjectStore = ECIndexedDbObjectStore(
            engagementCloudIndexedDb,
            objectStoreConfig,
            json,
            mockLogger,
            StandardTestDispatcher()
        )

        val event1 = SdkEvent.External.Custom(
            "id1",
            "name1",
            buildJsonObject { put("key1", JsonPrimitive("value1")) })

        val event2 = SdkEvent.External.Custom(
            "id2",
            "name2",
            buildJsonObject { put("key2", JsonPrimitive("value2")) })

        ECIndexedDbObjectStore.put("id1", event1)
        ECIndexedDbObjectStore.put("id2", event2)

        val events = ECIndexedDbObjectStore.getAll().toList()

        events.size shouldBe 2
        events[0] shouldBe event1
        events[1] shouldBe event2
    }

    @Test
    fun getAll_shouldReturn_dataThatCouldBeParsed() = runTest {
        clearDatabase()
        val mockJson: StringFormat = mock(MockMode.autofill)
        val objectStoreConfig = EngagementCloudObjectStoreConfig.ClientId
        val ECIndexedDbObjectStore = ECIndexedDbObjectStore(
            engagementCloudIndexedDb,
            objectStoreConfig,
            mockJson,
            mockLogger,
            StandardTestDispatcher()
        )

        val testClientId1 = "testClientId1"
        val testClientId2 = "testClientId2"
        every {
            mockJson.encodeToString(
                objectStoreConfig.serializer,
                testClientId1
            )
        } returns "\"$testClientId1\""
        every {
            mockJson.encodeToString(
                objectStoreConfig.serializer,
                testClientId2
            )
        } returns "\"$testClientId2\""
        every {
            mockJson.decodeFromString(
                objectStoreConfig.serializer,
                "\"$testClientId1\""
            )
        } returns testClientId1
        every {
            mockJson.decodeFromString(
                objectStoreConfig.serializer,
                "\"$testClientId2\""
            )
        } throws RuntimeException("test exception")

        ECIndexedDbObjectStore.put("id1", testClientId1)
        ECIndexedDbObjectStore.put("id2", testClientId2)

        val clientIds = ECIndexedDbObjectStore.getAll().toList()

        clientIds.size shouldBe 1
        clientIds[0] shouldBe testClientId1
    }

    @Test
    fun getAll_shouldReturnEmptyFlow_whenNoData() = runTest {
        clearDatabase()
        val objectStoreConfig = EngagementCloudObjectStoreConfig.Events
        val ECIndexedDbObjectStore = ECIndexedDbObjectStore(
            engagementCloudIndexedDb,
            objectStoreConfig,
            json,
            mockLogger,
            StandardTestDispatcher()
        )

        val events = ECIndexedDbObjectStore.getAll().toList()

        events.size shouldBe 0
    }

    @Test
    fun get_shouldReturnSdkEvent_whenItExists() = runTest {
        clearDatabase()
        val objectStoreConfig = EngagementCloudObjectStoreConfig.Events
        val ECIndexedDbObjectStore = ECIndexedDbObjectStore(
            engagementCloudIndexedDb,
            objectStoreConfig,
            json,
            mockLogger,
            StandardTestDispatcher()
        )

        val event = SdkEvent.External.Custom(
            "id",
            "name",
            buildJsonObject { put("key", JsonPrimitive("value")) })

        ECIndexedDbObjectStore.put("id", event)

        val result = ECIndexedDbObjectStore.get("id")

        result shouldBe event
    }

    @Test
    fun get_shouldReturnNull_whenSdkEventWithId_doesNotExist() = runTest {
        clearDatabase()
        val objectStoreConfig = EngagementCloudObjectStoreConfig.Events
        val ECIndexedDbObjectStore = ECIndexedDbObjectStore(
            engagementCloudIndexedDb,
            objectStoreConfig,
            json,
            mockLogger,
            StandardTestDispatcher()
        )

        val event = SdkEvent.External.Custom(
            "id",
            "name",
            buildJsonObject { put("key", JsonPrimitive("value")) })

        ECIndexedDbObjectStore.put("id", event)

        val result = ECIndexedDbObjectStore.get("differentId")

        result shouldBe null
    }

    @Test
    fun get_shouldReturnNull_whenClientId_doesNotExist() = runTest {
        clearDatabase()
        val objectStoreConfig = EngagementCloudObjectStoreConfig.ClientId
        val ECIndexedDbObjectStore = ECIndexedDbObjectStore(
            engagementCloudIndexedDb,
            objectStoreConfig,
            json,
            mockLogger,
            StandardTestDispatcher()
        )

        val testClientId = "testClientId"

        ECIndexedDbObjectStore.put("clientId", testClientId)

        val result = ECIndexedDbObjectStore.get("differentId")

        result shouldBe null
    }

    @Test
    fun delete_shouldDeleteSdkEvent_whenItExists() = runTest {
        clearDatabase()
        val objectStoreConfig = EngagementCloudObjectStoreConfig.Events
        val ECIndexedDbObjectStore = ECIndexedDbObjectStore(
            engagementCloudIndexedDb,
            objectStoreConfig,
            json,
            mockLogger,
            StandardTestDispatcher()
        )

        val event = SdkEvent.External.Custom(
            "id",
            "name",
            buildJsonObject { put("key", JsonPrimitive("value")) })

        ECIndexedDbObjectStore.put("id", event)

        ECIndexedDbObjectStore.delete("id")

        checkDb(null, EngagementCloudObjectStoreConfig.Events)
    }

    @Test
    fun delete_shouldDoNothing_whenSdkEventDoesNotExist() = runTest {
        clearDatabase()
        val objectStoreConfig = EngagementCloudObjectStoreConfig.Events
        val ECIndexedDbObjectStore = ECIndexedDbObjectStore(
            engagementCloudIndexedDb,
            objectStoreConfig,
            json,
            mockLogger,
            StandardTestDispatcher()
        )

        ECIndexedDbObjectStore.delete("notExistingId")

        checkDb(null, EngagementCloudObjectStoreConfig.Events)
    }

    private suspend fun <T> checkDb(
        expectedValue: T?,
        objectStoreConfig: EngagementCloudObjectStoreConfig<T>
    ) {
        return engagementCloudIndexedDb.execute { database ->
            suspendCoroutine { continuation ->
                val transaction = database
                    .transaction(objectStoreConfig.name, IDBTransactionMode.readonly)
                val request = transaction.objectStore(objectStoreConfig.name)
                    .get(IDBValidKey("id"))

                transaction.oncomplete = EventHandler {
                    if (expectedValue == null) {
                        request.result shouldBe null
                    } else {
                        JsonUtil.json.decodeFromString(
                            objectStoreConfig.serializer,
                            JSON.stringify(request.result)
                        ) shouldBe expectedValue
                    }
                    continuation.resume(Unit)
                }

                transaction.onerror = EventHandler {
                    continuation.resumeWithException(request.error!!)
                }
            }
        }
    }

    private suspend fun clearDatabase() {
        engagementCloudIndexedDb.execute { database ->
            database.objectStoreNames.asList().forEach {
                suspendCoroutine { continuation ->
                    val transaction = database.transaction(it, IDBTransactionMode.readwrite)
                    val request =
                        transaction.objectStore(it).clear()

                    transaction.oncomplete = EventHandler {
                        continuation.resume(Unit)
                    }

                    transaction.onerror = EventHandler {
                        continuation.resumeWithException(request.error!!)
                    }
                }
            }
        }
    }
}