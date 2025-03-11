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

    private var indexedDbInstance: IDBDatabase? = null

    suspend fun open(): IDBDatabase {
        sdkLogger.debug("EmarsysIndexedDb - open", "Opening indexedDB")
        return try {
            suspendCoroutine { continuation ->
                indexedDbInstance?.let {
                    continuation.resume(it)
                    return@suspendCoroutine
                }

                val openIndexedDBRequest =
                    indexedDBFactory.open(DATABASE_NAME, DATABASE_VERSION)

                openIndexedDBRequest.onsuccess = EventHandler {
                    indexedDbInstance = openIndexedDBRequest.result
                    continuation.resume(indexedDbInstance!!)
                }

                openIndexedDBRequest.onerror = EventHandler {
                    continuation.resumeWithException(openIndexedDBRequest.error!!)
                }

                openIndexedDBRequest.onupgradeneeded = EventHandler {
                    val database = openIndexedDBRequest.result
                    database.createObjectStore(
                        "events",
                        js("{}").unsafeCast<IDBObjectStoreParameters>().copy(keyPath = "id")
                    )
                }
            }
        } catch (exception: Exception) {
            sdkLogger.error("EmarsysIndexedDb - open", exception)
            throw exception
        }
    }

}