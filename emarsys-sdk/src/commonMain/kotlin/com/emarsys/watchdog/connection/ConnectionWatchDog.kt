package com.emarsys.watchdog.connection

import kotlinx.coroutines.flow.StateFlow

interface ConnectionWatchDog {
    val isOnline: StateFlow<Boolean>

    fun start()
}