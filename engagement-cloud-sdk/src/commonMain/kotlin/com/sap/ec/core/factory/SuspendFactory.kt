package com.sap.ec.core.factory

interface SuspendFactory<Input, Result> {

    suspend fun create(value: Input): Result
}
