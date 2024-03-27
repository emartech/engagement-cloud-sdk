package com.emarsys.watchdog.lifecycle

import com.emarsys.core.actions.LifecycleEvent
import kotlinx.coroutines.flow.SharedFlow

interface LifecycleWatchdog {
    val lifecycleEvents: SharedFlow<LifecycleEvent>
}