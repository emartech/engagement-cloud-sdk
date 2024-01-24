package com.emarsys.core

interface ObservableApi<Value> {

    suspend fun changeValue(newValue: Value)

    fun addObserver(observer: Observer<Value>)

    fun removeObserver(observer: Observer<Value>)
}