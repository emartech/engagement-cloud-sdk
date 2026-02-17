package com.sap.ec.watchdog.lifecycle

import com.sap.ec.core.Registerable
import com.sap.ec.core.lifecycle.LifecycleEvent
import kotlinx.coroutines.flow.SharedFlow

interface LifecycleWatchDog : Registerable {
    val lifecycleEvents: SharedFlow<LifecycleEvent>

}