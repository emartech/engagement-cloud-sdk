package com.emarsys.providers

interface Provider<Value> {
    
    fun provide(): Value
    
}