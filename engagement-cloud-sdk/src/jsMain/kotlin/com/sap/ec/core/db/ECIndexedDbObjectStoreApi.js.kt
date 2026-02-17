package com.sap.ec.core.db

import kotlinx.coroutines.flow.Flow

internal interface ECIndexedDbObjectStoreApi<T> {

    suspend fun put(id: String, value: T): String

    suspend fun getAll(): Flow<T>

    suspend fun delete(id: String)

    suspend fun removeAll()
}