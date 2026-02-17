package com.sap.ec.watchdog.connection

import com.sap.ec.core.Registerable
import kotlinx.coroutines.flow.StateFlow

interface ConnectionWatchDog : Registerable {
    val isOnline: StateFlow<Boolean>
}