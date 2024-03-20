package com.emarsys.connection

import kotlinx.coroutines.flow.StateFlow

interface ConnectionWatchDog {
    val isOnline: StateFlow<Boolean>

    fun start()
}