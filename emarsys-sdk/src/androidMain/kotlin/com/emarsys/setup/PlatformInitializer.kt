package com.emarsys.setup

import android.app.NotificationManager
import com.emarsys.di.SdkComponent
import com.emarsys.networking.clients.event.model.SdkEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

internal class PlatformInitializer(
    private val sdkEventFlow: MutableSharedFlow<SdkEvent>,
    private val notificationManager: NotificationManager,
    private val sdkDispatcher: CoroutineDispatcher
) : PlatformInitializerApi, SdkComponent {

    override suspend fun init() {
        CoroutineScope(sdkDispatcher).launch(start = CoroutineStart.UNDISPATCHED) {
            sdkEventFlow.filter { it is SdkEvent.Internal.Sdk.Dismiss }.collect {
                val event = it as SdkEvent.Internal.Sdk.Dismiss
                notificationManager.cancel(event.id, event.id.hashCode())
            }
        }
    }

}