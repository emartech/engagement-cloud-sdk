package com.emarsys.core.watchdog.lifecycle

import com.emarsys.core.actions.LifecycleEvent
import com.emarsys.watchdog.lifecycle.LifecycleWatchDog

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import platform.Foundation.NSNotificationCenter
import platform.UIKit.UIApplicationDidBecomeActiveNotification
import platform.UIKit.UIApplicationDidEnterBackgroundNotification

class IosLifecycleWatchdog : LifecycleWatchDog {

    private val _lifecycleEvents = MutableSharedFlow<LifecycleEvent>()
    override val lifecycleEvents: SharedFlow<LifecycleEvent> get() = _lifecycleEvents

    override suspend fun register() {
        NSNotificationCenter.defaultCenter.addObserverForName(
            name = UIApplicationDidBecomeActiveNotification,
            `object` = null,
            queue = null
        ) { _ ->
            notifyLifecycleEvent(LifecycleEvent.OnForeground)
        }

        NSNotificationCenter.defaultCenter.addObserverForName(
            name = UIApplicationDidEnterBackgroundNotification,
            `object` = null,
            queue = null
        ) { _ ->
            notifyLifecycleEvent(LifecycleEvent.OnBackground)
        }
    }

    private fun notifyLifecycleEvent(event: LifecycleEvent) {
        CoroutineScope(Dispatchers.Main).launch {
            _lifecycleEvents.emit(event)
        }
    }
}