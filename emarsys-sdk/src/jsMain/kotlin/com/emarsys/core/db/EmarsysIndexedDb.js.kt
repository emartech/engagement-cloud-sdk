package com.emarsys.core.db

import com.emarsys.core.log.Logger
import web.events.EventHandler
import web.idb.IDBDatabase
import web.idb.IDBFactory
import web.idb.IDBObjectStoreParameters
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class EmarsysIndexedDb(
    private val indexedDBFactory: IDBFactory,
    private val sdkLogger: Logger
) {

    companion object {
        private const val DATABASE_NAME = "EMARSYS_SDK_DB"
        private const val DATABASE_VERSION = 1.0
    }

    suspend fun <T> execute(block: suspend (IDBDatabase) -> T): T {
        sdkLogger.debug("Opening indexedDB")
        val database = try {
            suspendCoroutine<IDBDatabase> { continuation ->
                val openIndexedDBRequest =
                    indexedDBFactory.open(DATABASE_NAME, DATABASE_VERSION)

                openIndexedDBRequest.onsuccess = EventHandler {
                    continuation.resume(openIndexedDBRequest.result)
                }

                openIndexedDBRequest.onerror = EventHandler {
                    continuation.resumeWithException(openIndexedDBRequest.error!!)
                }

                openIndexedDBRequest.onupgradeneeded = EventHandler {
                    val database = openIndexedDBRequest.result
                    database.createObjectStore(
                        EmarsysObjectStoreConfig.Events.name,
                        js("{}").unsafeCast<IDBObjectStoreParameters>()
                    )
                    database.createObjectStore(
                        EmarsysObjectStoreConfig.ClientId.name,
                        js("{}").unsafeCast<IDBObjectStoreParameters>()
                    )
                }
            }
        } catch (exception: Exception) {
            sdkLogger.error("EmarsysIndexedDb - open", exception)
            throw exception
        }

        return try {
            block(database)
        } finally {
            database.close()
        }
    }

}