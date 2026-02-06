package com.emarsys.init.states

import com.emarsys.SdkConstants
import com.emarsys.api.push.PushConstants
import com.emarsys.context.SdkContextApi
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.context.RequestContextApi
import com.emarsys.fake.FakeStringStorage
import dev.mokkery.MockMode
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import web.events.EventHandler
import web.idb.IDBDatabase
import web.idb.IDBTransactionMode
import web.idb.indexedDB
import web.idb.readwrite
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test


@OptIn(ExperimentalWasmJsInterop::class)
class LegacySDKMigrationStateTests {

    private lateinit var mockRequestContext: RequestContextApi
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var fakeStringStorage: FakeStringStorage
    private lateinit var mockLogger: Logger
    private lateinit var migrationState: LegacySDKMigrationState

    private companion object {
        const val LEGACY_DB_NAME = "EMARSYS_WEBPUSH_STORE"
        const val STORE_KEY_VALUE = "keyValue"

        const val TEST_APP_CODE = "TESTCODE"
        const val TEST_CLIENT_ID = "test_client_id"
        const val TEST_CONTACT_TOKEN = "test_contact_token"
        const val TEST_REFRESH_TOKEN = "test_refresh_token"
        const val TEST_CLIENT_STATE = "test_client_state"
        const val TEST_PUSH_TOKEN = "test_push_token"
    }

    @BeforeTest
    fun setup() {
        mockRequestContext = mock(MockMode.autofill)
        mockSdkContext = mock(MockMode.autofill)
        fakeStringStorage = FakeStringStorage()
        mockLogger = mock(MockMode.autofill)
        migrationState = LegacySDKMigrationState(
            requestContext = mockRequestContext,
            sdkContext = mockSdkContext,
            stringStorage = fakeStringStorage,
            sdkLogger = mockLogger
        )
    }

    @AfterTest
    fun tearDown() = runTest {
        deleteLegacyDatabase()
    }

    @Test
    fun name_shouldReturn_legacySDKMigrationState() {
        migrationState.name shouldBe "legacySDKMigrationState"
    }

    @Test
    fun prepare_shouldNotThrow() {
        migrationState.prepare()
    }

    @Test
    fun relax_shouldNotThrow() {
        migrationState.relax()
    }

    @Test
    fun active_shouldNotReadFromIndexedDB_whenMigrationAlreadyDone() = runTest {
        fakeStringStorage.put(SdkConstants.LEGACY_SDK_MIGRATION_DONE_KEY, "true")

        migrationState.active()

        verify(VerifyMode.exactly(0)) { mockRequestContext.contactToken = any() }
        verify(VerifyMode.exactly(0)) { mockRequestContext.refreshToken = any() }
        verify(VerifyMode.exactly(0)) { mockRequestContext.clientState = any() }
    }

    @Test
    fun active_shouldSetMigrationDone() = runTest {
        val result = migrationState.active()

        fakeStringStorage.get(SdkConstants.LEGACY_SDK_MIGRATION_DONE_KEY) shouldBe "true"
        result shouldBe Result.success(Unit)
    }

    @Test
    fun active_shouldNotSetClientId_whenNoLegacyDatabaseExists() = runTest {
        migrationState.active()

        fakeStringStorage.get(SdkConstants.CLIENT_ID_STORAGE_KEY) shouldBe null
        verify(VerifyMode.exactly(0)) { mockRequestContext.contactToken = any() }
    }

    @Test
    fun active_shouldNotSetRefreshToken_whenNoLegacyDatabaseExists() = runTest {
        migrationState.active()

        verify(VerifyMode.exactly(0)) { mockRequestContext.refreshToken = any() }
    }

    @Test
    fun active_shouldNotSetClientState_whenNoLegacyDatabaseExists() = runTest {
        migrationState.active()

        verify(VerifyMode.exactly(0)) { mockRequestContext.clientState = any() }
    }

    @Test
    fun active_shouldNotSetPushToken_whenNoLegacyDatabaseExists() = runTest {
        migrationState.active()

        fakeStringStorage.get(PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY) shouldBe null
    }

    @Test
    fun active_shouldReturn_whenMigrationAlreadyDone() = runTest {
        fakeStringStorage.put(SdkConstants.LEGACY_SDK_MIGRATION_DONE_KEY, "true")

        val result = migrationState.active()

        result shouldBe Result.success(Unit)

        verify(VerifyMode.exactly(0)) { mockRequestContext.contactToken = any() }
        verify(VerifyMode.exactly(0)) { mockRequestContext.refreshToken = any() }
        verify(VerifyMode.exactly(0)) { mockRequestContext.clientState = any() }
    }

    @Test
    fun active_shouldMigrateClientId_fromIndexedDB() = runTest {
        setupLegacyDatabase(
            "browserIds" to """{"$TEST_APP_CODE":"$TEST_CLIENT_ID"}""",
            "emarsysApplicationCode" to TEST_APP_CODE
        )

        migrationState.active()

        fakeStringStorage.get(SdkConstants.CLIENT_ID_STORAGE_KEY) shouldBe TEST_CLIENT_ID
    }

    @Test
    fun active_shouldMigrateContactToken_fromIndexedDB() = runTest {
        setupLegacyDatabase("contactToken" to TEST_CONTACT_TOKEN)

        migrationState.active()

        verify { mockRequestContext.contactToken = TEST_CONTACT_TOKEN }
    }

    @Test
    fun active_shouldMigrateRefreshToken_fromIndexedDB() = runTest {
        setupLegacyDatabase("refreshToken" to TEST_REFRESH_TOKEN)

        migrationState.active()

        verify { mockRequestContext.refreshToken = TEST_REFRESH_TOKEN }
    }

    @Test
    fun active_shouldMigrateClientState_fromIndexedDB() = runTest {
        setupLegacyDatabase("xClientState" to TEST_CLIENT_STATE)

        migrationState.active()

        verify { mockRequestContext.clientState = TEST_CLIENT_STATE }
    }

    @Test
    fun active_shouldMigratePushToken_fromIndexedDB() = runTest {
        setupLegacyDatabase("pushToken" to TEST_PUSH_TOKEN)

        migrationState.active()

        fakeStringStorage.get(PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY) shouldBe TEST_PUSH_TOKEN
    }

    @Test
    fun active_shouldMigrateAllValues_whenAllExist() = runTest {
        setupLegacyDatabase(
            "browserIds" to """{"$TEST_APP_CODE":"$TEST_CLIENT_ID"}""",
            "emarsysApplicationCode" to TEST_APP_CODE,
            "contactToken" to TEST_CONTACT_TOKEN,
            "refreshToken" to TEST_REFRESH_TOKEN,
            "xClientState" to TEST_CLIENT_STATE,
            "pushToken" to TEST_PUSH_TOKEN
        )

        migrationState.active()

        fakeStringStorage.get(SdkConstants.CLIENT_ID_STORAGE_KEY) shouldBe TEST_CLIENT_ID
        verify { mockRequestContext.contactToken = TEST_CONTACT_TOKEN }
        verify { mockRequestContext.refreshToken = TEST_REFRESH_TOKEN }
        verify { mockRequestContext.clientState = TEST_CLIENT_STATE }
        fakeStringStorage.get(PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY) shouldBe TEST_PUSH_TOKEN
        fakeStringStorage.get(SdkConstants.LEGACY_SDK_MIGRATION_DONE_KEY) shouldBe "true"
    }

    @Test
    fun active_shouldNotSetClientId_whenBrowserIdsIsMissing() = runTest {
        setupLegacyDatabase("emarsysApplicationCode" to TEST_APP_CODE)

        migrationState.active()

        fakeStringStorage.get(SdkConstants.CLIENT_ID_STORAGE_KEY) shouldBe null
    }

    @Test
    fun active_shouldNotSetClientId_whenApplicationCodeIsMissing() = runTest {
        setupLegacyDatabase("browserIds" to """{"$TEST_APP_CODE":"$TEST_CLIENT_ID"}""")

        migrationState.active()

        fakeStringStorage.get(SdkConstants.CLIENT_ID_STORAGE_KEY) shouldBe null
    }

    @Test
    fun active_shouldNotSetClientId_whenAppCodeNotInBrowserIds() = runTest {
        setupLegacyDatabase(
            "browserIds" to """{"OTHER_CODE":"other_client_id"}""",
            "emarsysApplicationCode" to TEST_APP_CODE
        )

        migrationState.active()

        fakeStringStorage.get(SdkConstants.CLIENT_ID_STORAGE_KEY) shouldBe null
    }

    private suspend fun setupLegacyDatabase(vararg entries: Pair<String, String>) {
        val db = openOrCreateLegacyDatabase()
        try {
            for ((key, value) in entries) {
                putValue(db, key, value)
            }
        } finally {
            db.close()
        }
    }

    private suspend fun openOrCreateLegacyDatabase(): IDBDatabase = suspendCoroutine { cont ->
        val request = indexedDB.open(LEGACY_DB_NAME, 1.0)
        request.onupgradeneeded = EventHandler {
            val db = request.result
            if (!js("db.objectStoreNames.contains('$STORE_KEY_VALUE')").unsafeCast<Boolean>()) {
                db.createObjectStore(STORE_KEY_VALUE, js("{ keyPath: 'key' }"))
            }
        }
        request.onsuccess = EventHandler { cont.resume(request.result) }
        request.onerror = EventHandler {
            throw Exception("Failed to open legacy database")
        }
    }

    private suspend fun putValue(db: IDBDatabase, key: String, value: String): Unit =
        suspendCoroutine { cont ->
            val transaction = db.transaction(arrayOf(STORE_KEY_VALUE), IDBTransactionMode.readwrite)
            val store = transaction.objectStore(STORE_KEY_VALUE)
            store.put(js("{ key: key, value: value }"))
            transaction.oncomplete = EventHandler { cont.resume(Unit) }
            transaction.onerror = EventHandler { cont.resume(Unit) }
        }

    private suspend fun deleteLegacyDatabase(): Unit = suspendCoroutine { cont ->
        val request = indexedDB.deleteDatabase(LEGACY_DB_NAME)
        request.onsuccess = EventHandler { cont.resume(Unit) }
        request.onerror = EventHandler { cont.resume(Unit) }
        request.onblocked = EventHandler { cont.resume(Unit) }
    }
}
