package com.emarsys.url

interface FactoryApi<Input, Output> {

    fun create(value: Input): Output
}