package com.emarsys.enable

import android.app.NotificationManager
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.event.SdkEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

internal class PlatformInitializer(
    private val sdkEventDistributor: SdkEventDistributorApi,
    private val notificationManager: NotificationManager,
    private val sdkDispatcher: CoroutineDispatcher
) : PlatformInitializerApi {

    override suspend fun init() {
        CoroutineScope(sdkDispatcher).launch(start = CoroutineStart.UNDISPATCHED) {
            sdkEventDistributor.sdkEventFlow.filter { it is SdkEvent.Internal.Sdk.Dismiss }.collect {
                val event = it as SdkEvent.Internal.Sdk.Dismiss
                notificationManager.cancel(event.id, event.id.hashCode())
            }
        }
    }

}