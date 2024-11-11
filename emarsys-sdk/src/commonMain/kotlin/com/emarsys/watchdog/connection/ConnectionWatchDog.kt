package com.emarsys.watchdog.connection

import com.emarsys.core.Registerable
import kotlinx.coroutines.flow.StateFlow

interface ConnectionWatchDog : Registerable {
    val isOnline: StateFlow<Boolean>
}