package com.emarsys.core.db

import kotlinx.coroutines.flow.Flow

interface EmarsysIndexedDbObjectStoreApi<T> {

    suspend fun put(id: String, value: T): String

    suspend fun getAll(): Flow<T>

    suspend fun delete(id: String)
}