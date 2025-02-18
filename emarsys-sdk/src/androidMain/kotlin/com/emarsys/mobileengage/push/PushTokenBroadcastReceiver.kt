package com.emarsys.mobileengage.push

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.emarsys.api.push.PushConstants
import com.emarsys.core.extension.goAsync
import com.emarsys.di.DependencyContainerPrivateApi
import com.emarsys.di.DependencyInjection
import kotlinx.coroutines.CoroutineDispatcher

class PushTokenBroadcastReceiver : BroadcastReceiver() {
    private val sdkDispatcher: CoroutineDispatcher =
        (DependencyInjection.container as DependencyContainerPrivateApi).sdkDispatcher

    override fun onReceive(context: Context, intent: Intent) = goAsync(sdkDispatcher) {
        intent.getStringExtra(PushConstants.PUSH_TOKEN_INTENT_KEY)?.let {
            with((DependencyInjection.container as DependencyContainerPrivateApi)) {
                stringStorage.put(PushConstants.PUSH_TOKEN_STORAGE_KEY, it)
                pushApi.registerPushToken(it)
            }
        }
    }
}
