package com.sap.ec.core.collections

interface ThreadSafePersistentStoreApi<Item> {
    var items: MutableList<Item>

    suspend fun add(item: Item)

    suspend fun dequeue(action: suspend (item: Item) -> Unit)
}