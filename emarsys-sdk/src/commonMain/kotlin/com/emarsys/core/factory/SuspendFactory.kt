package com.emarsys.core.factory

interface SuspendFactory<Input, Result> {

    suspend fun create(value: Input): Result
}
