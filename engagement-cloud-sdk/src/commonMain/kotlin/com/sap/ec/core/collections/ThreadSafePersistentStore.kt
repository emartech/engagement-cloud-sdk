package com.sap.ec.core.collections

import com.sap.ec.core.storage.StorageApi
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer

internal class ThreadSafePersistentStore<Item>(
    private val id: String,
    private val storage: StorageApi,
    itemSerializer: KSerializer<Item>
) : ThreadSafePersistentStoreApi<Item> {

    private val itemsSerializer = ListSerializer(itemSerializer)
    private val mutex = Mutex()
    override var items: MutableList<Item> =
        storage.get(id, itemsSerializer)?.toMutableList() ?: mutableListOf()

    private fun persist() {
        storage.put(id, itemsSerializer, this.items)
    }

    override suspend fun add(item: Item) {
        mutex.withLock {
            this.items.add(item)
            persist()
        }
    }

    override suspend fun dequeue(action: suspend (call: Item) -> Unit) {
        mutex.withLock {
            this.items.dequeue {
                action(it)
            }
            persist()
        }
    }
}
