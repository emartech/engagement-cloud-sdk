package com.emarsys.core.db

import com.emarsys.core.log.Logger
import com.emarsys.event.SdkEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.StringFormat
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import web.events.EventHandler
import web.idb.IDBTransactionMode
import web.idb.IDBValidKey
import web.idb.readonly
import web.idb.readwrite
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class EmarsysIndexedDbObjectStore<T>(
    private val emarsysIndexedDb: EmarsysIndexedDb,
    private val emarsysObjectStoreConfig: EmarsysObjectStoreConfig<T>,
    private val json: StringFormat,
    private val logger: Logger,
    private val sdkDispatcher: CoroutineDispatcher,
) : EmarsysIndexedDbObjectStoreApi<T> {

    override suspend fun put(id: String, value: T): String {
        return emarsysIndexedDb.execute { database ->
            val savedId = suspendCoroutine { continuation ->
                val transaction = database.transaction(
                    emarsysObjectStoreConfig.name,
                    IDBTransactionMode.readwrite
                )
                val request = transaction.objectStore(emarsysObjectStoreConfig.name)
                    .put(
                        JSON.parse(
                            json.encodeToString(
                                emarsysObjectStoreConfig.serializer,
                                value
                            )
                        ),
                        IDBValidKey(id)
                    )

                transaction.oncomplete = EventHandler {
                    continuation.resume(request.result.unsafeCast<String>())
                }

                transaction.onerror = EventHandler {
                    CoroutineScope(sdkDispatcher).launch {
                        logger.error("EmarsysIndexedDbObjectStore - put", buildJsonObject {
                            put("value", JsonPrimitive(value.toString()))
                            put("id", JsonPrimitive(id))
                        }, isRemoteLog = value !is SdkEvent.Internal.LogEvent)
                    }
                    continuation.resumeWithException(request.error!!)
                }
            }
            logger.debug("EmarsysIndexedDbObjectStore - put", buildJsonObject {
                put("value", value.toString())
                put("id", id)
            }, isRemoteLog = value !is SdkEvent.Internal.LogEvent)
            savedId
        }
    }

    override suspend fun getAll(): Flow<T> {
        return emarsysIndexedDb.execute { database ->
            logger.debug("Fetching all data from store: ${emarsysObjectStoreConfig.name}")
            suspendCoroutine { continuation ->
                val transaction = database.transaction(
                    emarsysObjectStoreConfig.name,
                    IDBTransactionMode.readonly
                )
                val request = transaction.objectStore(emarsysObjectStoreConfig.name)
                    .getAll()

                transaction.oncomplete = EventHandler {
                    val result = request.result.unsafeCast<Array<Any>>()
                        .mapNotNull {
                            try {
                                json.decodeFromString(
                                    emarsysObjectStoreConfig.serializer,
                                    JSON.stringify(it)
                                )
                            } catch (exception: Exception) {
                                null
                            }
                        }
                    continuation.resume(result)
                }

                transaction.onerror = EventHandler {
                    CoroutineScope(sdkDispatcher).launch {
                        logger.error("Failed to retrieve data from store: ${emarsysObjectStoreConfig.name}")
                    }
                    continuation.resumeWithException(request.error!!)
                }
            }.asFlow()
        }

    }

    suspend fun get(id: String): T? {
        return emarsysIndexedDb.execute { database ->
            logger.debug("EmarsysIndexedDbObjectStore - get", buildJsonObject {
                put("id", id)
            })
            suspendCoroutine { continuation ->
                val transaction = database.transaction(
                    emarsysObjectStoreConfig.name,
                    IDBTransactionMode.readonly
                )

                val request =
                    transaction.objectStore(emarsysObjectStoreConfig.name).get(IDBValidKey(id))

                transaction.oncomplete = EventHandler {
                    val result = request.result?.let {
                        try {
                            json.decodeFromString(
                                emarsysObjectStoreConfig.serializer,
                                JSON.stringify(request.result)
                            )
                        } catch (exception: Exception) {
                            continuation.resumeWithException(exception)
                            return@EventHandler
                        }
                    }
                    continuation.resume(result.unsafeCast<T?>())
                }

                transaction.onerror = EventHandler {
                    CoroutineScope(sdkDispatcher).launch {
                        logger.error(
                            "EmarsysIndexedDbObjectStore - get",
                            buildJsonObject {
                                put("id", JsonPrimitive(id))
                            }
                        )
                    }
                    continuation.resumeWithException(request.error!!)
                }
            }
        }

    }

    override suspend fun delete(id: String) {
        return emarsysIndexedDb.execute { database ->
            logger.debug("EmarsysIndexedDbObjectStore - delete", buildJsonObject {
                put("id", id)
            })
            suspendCoroutine { continuation ->
                val transaction = database.transaction(
                    emarsysObjectStoreConfig.name,
                    IDBTransactionMode.readwrite
                )

                val request =
                    transaction.objectStore(emarsysObjectStoreConfig.name).delete(IDBValidKey(id))

                transaction.oncomplete = EventHandler {
                    continuation.resume(Unit)
                }

                transaction.onerror = EventHandler {
                    CoroutineScope(sdkDispatcher).launch {
                        logger.error(
                            "delete",
                            buildJsonObject {
                                put("id", id)
                            }
                        )
                    }
                    continuation.resumeWithException(request.error!!)
                }
            }
        }
    }

    override suspend fun removeAll() {
        return emarsysIndexedDb.execute { database ->
            suspendCoroutine { continuation ->
                val transaction = database.transaction(
                    emarsysObjectStoreConfig.name,
                    IDBTransactionMode.readwrite
                )

                val request =
                    transaction.objectStore(emarsysObjectStoreConfig.name).clear()

                transaction.oncomplete = EventHandler {
                    continuation.resume(Unit)
                }

                transaction.onerror = EventHandler {
                    CoroutineScope(sdkDispatcher).launch {
                        logger.error("clear")
                    }
                    continuation.resumeWithException(request.error!!)
                }
            }
        }
    }
}