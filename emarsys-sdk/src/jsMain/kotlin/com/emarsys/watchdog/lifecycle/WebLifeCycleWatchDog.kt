package com.emarsys.watchdog.lifecycle

import com.emarsys.core.lifecycle.LifecycleEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import web.dom.Document
import web.dom.DocumentVisibilityState
import web.dom.visible
import web.events.Event
import web.events.EventType
import web.events.addEventListener

class WebLifeCycleWatchDog(
    private val document: Document,
    private val lifeCycleWatchDogScope: CoroutineScope
) : LifecycleWatchDog {

    private val _lifecycleEvents = MutableSharedFlow<LifecycleEvent>()

    override val lifecycleEvents: SharedFlow<LifecycleEvent> = _lifecycleEvents.asSharedFlow()

    override suspend fun register() {
        document.addEventListener(EventType("visibilitychange"), this::onVisibilityChange)
        document.addEventListener(EventType("pagehide"), this::onPageHide)
    }

    private fun onVisibilityChange(event: Event) {
        lifeCycleWatchDogScope.launch {
            _lifecycleEvents.emit(mapVisibilityStateToLifeCycleEvent(document.visibilityState))
        }
    }

    private fun mapVisibilityStateToLifeCycleEvent(visibilityState: DocumentVisibilityState) =
        if (visibilityState == DocumentVisibilityState.visible)
            LifecycleEvent.OnForeground
        else
            LifecycleEvent.OnBackground

    private fun onPageHide(event: Event) {
        lifeCycleWatchDogScope.launch {
            _lifecycleEvents.emit(LifecycleEvent.OnBackground)
        }
    }
}