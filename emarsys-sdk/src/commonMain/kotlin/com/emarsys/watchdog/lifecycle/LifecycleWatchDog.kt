package com.emarsys.watchdog.lifecycle

import com.emarsys.core.Registerable
import com.emarsys.core.lifecycle.LifecycleEvent
import kotlinx.coroutines.flow.SharedFlow

interface LifecycleWatchDog : Registerable {
    val lifecycleEvents: SharedFlow<LifecycleEvent>

}