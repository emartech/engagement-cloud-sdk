package com.emarsys.core.providers

interface Provider<Value> {

    fun provide(): Value
}

interface SuspendProvider<Value> {

    suspend fun provide(): Value
}