package com.sap.ec.core.factory

internal interface Factory<Input, Result> {

    fun create(value: Input): Result
}
