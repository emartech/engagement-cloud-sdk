package com.emarsys.watchdog.lifecycle

import com.emarsys.core.actions.LifecycleEvent
import com.emarsys.watchdog.Registerable
import kotlinx.coroutines.flow.SharedFlow

interface LifecycleWatchDog : Registerable {
    val lifecycleEvents: SharedFlow<LifecycleEvent>

}