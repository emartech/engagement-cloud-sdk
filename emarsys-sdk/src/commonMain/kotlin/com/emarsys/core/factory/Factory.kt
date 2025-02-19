package com.emarsys.core.factory

interface Factory<Input, Result> {

    fun create(value: Input): Result
}
