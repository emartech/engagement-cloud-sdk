package com.emarsys.core.providers

interface Provider<Value> {
    
    fun provide(): Value
}