package com.sap.ec.init.states

import com.sap.ec.SdkConstants
import com.sap.ec.api.push.PushConstants
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.context.RequestContextApi
import com.sap.ec.core.state.State
import com.sap.ec.core.storage.StringStorageApi
import com.sap.ec.util.runCatchingWithoutCancellation
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import web.events.EventHandler
import web.idb.IDBDatabase
import web.idb.IDBTransactionMode
import web.idb.IDBValidKey
import web.idb.indexedDB
import web.idb.readonly
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
internal actual class LegacySDKMigrationState(
    private val requestContext: RequestContextApi,
    private val sdkContext: SdkContextApi,
    private val stringStorage: StringStorageApi,
    private val sdkLogger: Logger
) : State {

    private companion object {
        const val LEGACY_DB_NAME = "EMARSYS_WEBPUSH_STORE"
        const val LEGACY_DB_VERSION = 1.0
        const val STORE_KEY_VALUE = "keyValue"

        const val KEY_BROWSER_IDS = "browserIds"
        const val KEY_APPLICATION_CODE = "emarsysApplicationCode"
        const val KEY_PUSH_TOKEN = "pushToken"
        const val KEY_CONTACT_TOKEN = "contactToken"
        const val KEY_REFRESH_TOKEN = "refreshToken"
        const val KEY_CLIENT_STATE = "xClientState"
    }

    actual override val name: String = "legacySDKMigrationState"

    actual override fun prepare() {}

    actual override suspend fun active(): Result<Unit> {
        if (isMigrationAlreadyDone()) {
            return Result.success(Unit)
        }

        sdkLogger.debug("Starting migration from legacy Web SDK")

        runCatchingWithoutCancellation {
            val db = openLegacyDatabase() ?: return@runCatchingWithoutCancellation
            try {
                if (!hasKeyValueStore(db)) return@runCatchingWithoutCancellation
                migrateClientId(db)
                migrateContactToken(db)
                migrateRefreshToken(db)
                migrateClientState(db)
                migratePushToken(db)
            } finally {
                db.close()
            }
        }.onFailure {
            sdkLogger.error("Migration from legacy Web SDK failed: ${it.message}")
        }

        stringStorage.put(SdkConstants.LEGACY_SDK_MIGRATION_DONE_KEY, "true")
        return Result.success(Unit)
    }

    actual override fun relax() {}

    private fun isMigrationAlreadyDone(): Boolean =
        stringStorage.get(SdkConstants.LEGACY_SDK_MIGRATION_DONE_KEY) != null


    private suspend fun openLegacyDatabase(): IDBDatabase? = try {
        openIndexedDB(LEGACY_DB_NAME, LEGACY_DB_VERSION)
    } catch (e: Exception) {
        sdkLogger.debug("Legacy IndexedDB not found or error opening: ${e.message}")
        null
    }

    private fun hasKeyValueStore(db: IDBDatabase): Boolean =
        js("db.objectStoreNames.contains('keyValue')") as Boolean

    private suspend fun migrateClientId(db: IDBDatabase) {
        runCatchingWithoutCancellation {
            val browserIdsJson = getValue(db, KEY_BROWSER_IDS) ?: return
            val appCode = getValue(db, KEY_APPLICATION_CODE) ?: return

            runCatching {
                val jsonElement = Json.parseToJsonElement(browserIdsJson)
                (jsonElement as? JsonObject)
                    ?.get(appCode.uppercase())
                    ?.jsonPrimitive
                    ?.content
            }.onSuccess { clientId ->
                clientId?.let {
                    stringStorage.put(SdkConstants.CLIENT_ID_STORAGE_KEY, it)
                    sdkLogger.debug("Migrated legacy clientId.")
                }
            }.onFailure { e ->
                sdkLogger.error("Failed to parse browserIds: ${e.message}")
            }
        }
    }

    private suspend fun migrateContactToken(db: IDBDatabase) {
        runCatchingWithoutCancellation {
            getValue(db, KEY_CONTACT_TOKEN)?.let {
                requestContext.contactToken = it
                sdkLogger.debug("Migrated legacy contactToken")
            }
        }
    }

    private suspend fun migrateRefreshToken(db: IDBDatabase) {
        runCatchingWithoutCancellation {
            getValue(db, KEY_REFRESH_TOKEN)?.let {
                requestContext.refreshToken = it
                sdkLogger.debug("Migrated legacy refreshToken")
            }
        }
    }

    private suspend fun migrateClientState(db: IDBDatabase) {
        runCatchingWithoutCancellation {
            getValue(db, KEY_CLIENT_STATE)?.let {
                requestContext.clientState = it
                sdkLogger.debug("Migrated legacy clientState")
            }
        }
    }

    private suspend fun migratePushToken(db: IDBDatabase) {
        runCatchingWithoutCancellation {
            getValue(db, KEY_PUSH_TOKEN)?.let {
                stringStorage.put(PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY, it)
                sdkLogger.debug("Migrated legacy pushToken")
            }
        }
    }

    private suspend fun openIndexedDB(name: String, version: Double): IDBDatabase =
        suspendCoroutine { cont ->
            val request = indexedDB.open(name, version)
            request.onsuccess = EventHandler { cont.resume(request.result) }
            request.onerror = EventHandler {
                cont.resumeWithException(Exception("Failed to open IndexedDB: $name"))
            }
        }

    private suspend fun getValue(db: IDBDatabase, key: String): String? = suspendCoroutine { cont ->
        if (!hasKeyValueStore(db)) {
            cont.resume(null)
            return@suspendCoroutine
        }

        val transaction = db.transaction(arrayOf(STORE_KEY_VALUE), IDBTransactionMode.readonly)
        val store = transaction.objectStore(STORE_KEY_VALUE)
        val request = store.get(IDBValidKey(key))

        transaction.oncomplete = EventHandler {
            cont.resume(parseResult(request.result))
        }
        transaction.onerror = EventHandler {
            cont.resume(null)
        }
    }

    private fun parseResult(result: Any?): String? {
        val value = result?.asDynamic()?.value ?: return null
        if (value == null || value == undefined) return null

        return when (js("typeof value") as String) {
            "string" -> value as String
            else -> JSON.stringify(value)
        }
    }

}
