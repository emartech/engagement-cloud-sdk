package com.emarsys.watchdog.connection

import com.emarsys.watchdog.Registerable
import kotlinx.coroutines.flow.StateFlow

interface ConnectionWatchDog : Registerable {
    val isOnline: StateFlow<Boolean>
}