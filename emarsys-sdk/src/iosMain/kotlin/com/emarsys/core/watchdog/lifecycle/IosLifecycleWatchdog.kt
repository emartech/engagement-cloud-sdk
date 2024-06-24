package com.emarsys.core.watchdog.lifecycle

import com.emarsys.core.actions.LifecycleEvent
import com.emarsys.watchdog.lifecycle.LifecycleWatchDog
import kotlinx.coroutines.flow.SharedFlow

class IosLifecycleWatchdog: LifecycleWatchDog {
    // mapping of lifecycles https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-lifecycle.html#ios
    override val lifecycleEvents: SharedFlow<LifecycleEvent>
        get() = TODO("Not yet implemented")

    override suspend fun register() {
        TODO("Not yet implemented")
    }
}