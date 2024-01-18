package com.emarsys.core

interface FactoryApi<Input, Output> {

    fun create(value: Input): Output
}