package com.emarsys.watchdog.lifecycle

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.emarsys.core.actions.LifecycleEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class AndroidLifecycleWatchDog(
    private val processLifecycleOwnerLifecycle: Lifecycle,
    private val processLifecycleOwnerScope: CoroutineScope,
    private val lifecycleWatchDogScope: CoroutineScope
) : DefaultLifecycleObserver, LifecycleWatchDog {

    private val _lifecycleEvents = MutableSharedFlow<LifecycleEvent>()

    override val lifecycleEvents: SharedFlow<LifecycleEvent> = _lifecycleEvents.asSharedFlow()

    override suspend fun register() {
        processLifecycleOwnerScope.launch {
            processLifecycleOwnerLifecycle.addObserver(this@AndroidLifecycleWatchDog)
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        lifecycleWatchDogScope.launch {
            _lifecycleEvents.emit(LifecycleEvent.OnForeground)
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        lifecycleWatchDogScope.launch {
            _lifecycleEvents.emit(LifecycleEvent.OnBackground)
        }
    }
}