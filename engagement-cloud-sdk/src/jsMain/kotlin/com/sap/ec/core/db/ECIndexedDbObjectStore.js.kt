package com.sap.ec.core.db

import com.sap.ec.core.log.Logger
import com.sap.ec.event.SdkEvent
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

@OptIn(ExperimentalWasmJsInterop::class)
class ECIndexedDbObjectStore<T>(
    private val engagementCloudIndexedDb: EngagementCloudIndexedDb,
    private val engagementCloudObjectStoreConfig: EngagementCloudObjectStoreConfig<T>,
    private val json: StringFormat,
    private val logger: Logger,
    private val sdkDispatcher: CoroutineDispatcher,
) : ECIndexedDbObjectStoreApi<T> {

    override suspend fun put(id: String, value: T): String {
        return engagementCloudIndexedDb.execute { database ->
            val savedId = suspendCoroutine { continuation ->
                val transaction = database.transaction(
                    engagementCloudObjectStoreConfig.name,
                    IDBTransactionMode.readwrite
                )
                val request = transaction.objectStore(engagementCloudObjectStoreConfig.name)
                    .put(
                        JSON.parse(
                            json.encodeToString(
                                engagementCloudObjectStoreConfig.serializer,
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
            savedId
        }
    }

    override suspend fun getAll(): Flow<T> {
        return engagementCloudIndexedDb.execute { database ->
            suspendCoroutine { continuation ->
                val transaction = database.transaction(
                    engagementCloudObjectStoreConfig.name,
                    IDBTransactionMode.readonly
                )
                val request = transaction.objectStore(engagementCloudObjectStoreConfig.name)
                    .getAll()

                transaction.oncomplete = EventHandler {
                    val result = request.result.unsafeCast<Array<Any>>()
                        .mapNotNull {
                            try {
                                json.decodeFromString(
                                    engagementCloudObjectStoreConfig.serializer,
                                    JSON.stringify(it)
                                )
                            } catch (_: Exception) {
                                null
                            }
                        }
                    continuation.resume(result)
                }

                transaction.onerror = EventHandler {
                    CoroutineScope(sdkDispatcher).launch {
                        logger.error("Failed to retrieve data from store: ${engagementCloudObjectStoreConfig.name}")
                    }
                    continuation.resumeWithException(request.error!!)
                }
            }.asFlow()
        }

    }

    suspend fun get(id: String): T? {
        return engagementCloudIndexedDb.execute { database ->
            suspendCoroutine { continuation ->
                val transaction = database.transaction(
                    engagementCloudObjectStoreConfig.name,
                    IDBTransactionMode.readonly
                )

                val request =
                    transaction.objectStore(engagementCloudObjectStoreConfig.name).get(IDBValidKey(id))

                transaction.oncomplete = EventHandler {
                    val result = request.result?.let {
                        try {
                            json.decodeFromString(
                                engagementCloudObjectStoreConfig.serializer,
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
        return engagementCloudIndexedDb.execute { database ->
            suspendCoroutine { continuation ->
                val transaction = database.transaction(
                    engagementCloudObjectStoreConfig.name,
                    IDBTransactionMode.readwrite
                )

                val request =
                    transaction.objectStore(engagementCloudObjectStoreConfig.name).delete(IDBValidKey(id))

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
        return engagementCloudIndexedDb.execute { database ->
            suspendCoroutine { continuation ->
                val transaction = database.transaction(
                    engagementCloudObjectStoreConfig.name,
                    IDBTransactionMode.readwrite
                )

                val request =
                    transaction.objectStore(engagementCloudObjectStoreConfig.name).clear()

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