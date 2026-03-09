package com.sap.ec.core.providers

internal interface Provider<Value> {

    fun provide(): Value
}

internal interface SuspendProvider<Value> {

    suspend fun provide(): Value
}