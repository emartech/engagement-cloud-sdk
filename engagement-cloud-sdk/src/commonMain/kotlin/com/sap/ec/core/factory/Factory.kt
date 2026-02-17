package com.sap.ec.core.factory

interface Factory<Input, Result> {

    fun create(value: Input): Result
}
