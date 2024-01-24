package com.emarsys.core

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

typealias Observer<Value> = suspend (value: Value) -> (Unit)
abstract class Observable<Value>(defaultValue: Value): ObservableApi<Value> {

    var value = defaultValue
        private set

    private var observers: MutableList<Observer<Value>> = mutableListOf()

    private val changeMutex = Mutex()
    override suspend fun changeValue(newValue: Value) {
        changeMutex.withLock {
            value = newValue
            observers.forEach {
                it.invoke(value)
            }
        }
    }

    override fun addObserver(observer: Observer<Value>) {
        observers.add(observer)
    }

    override fun removeObserver(observer: Observer<Value>) {
        observers.remove(observer)
    }

}