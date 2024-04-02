package com.emarsys.watchdog.lifecycle

import com.emarsys.core.actions.LifecycleEvent
import kotlinx.coroutines.flow.SharedFlow

interface LifecycleWatchDog {
    val lifecycleEvents: SharedFlow<LifecycleEvent>

    suspend fun start()
}