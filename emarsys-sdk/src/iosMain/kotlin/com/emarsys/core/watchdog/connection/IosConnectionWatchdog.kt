package com.emarsys.core.watchdog.connection

import com.emarsys.watchdog.connection.ConnectionWatchDog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class IosConnectionWatchdog: ConnectionWatchDog {
    private val _isOnline = MutableStateFlow(true)

    override val isOnline = _isOnline.asStateFlow()

    override suspend fun register() {
        TODO("Not yet implemented")
    }
}