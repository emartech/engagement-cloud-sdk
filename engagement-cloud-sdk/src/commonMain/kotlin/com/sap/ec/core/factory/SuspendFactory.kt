package com.sap.ec.core.factory

internal interface SuspendFactory<Input, Result> {

    suspend fun create(value: Input): Result
}
