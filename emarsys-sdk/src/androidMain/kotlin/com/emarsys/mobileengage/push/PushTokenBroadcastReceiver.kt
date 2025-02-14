package com.emarsys.mobileengage.push

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.emarsys.api.push.PushApi
import com.emarsys.api.push.PushConstants
import com.emarsys.core.extension.goAsync
import kotlinx.coroutines.CoroutineDispatcher

class PushTokenBroadcastReceiver(
    private val sdkDispatcher: CoroutineDispatcher,
    private val pushApi: PushApi
) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = goAsync(sdkDispatcher) {
        intent.getStringExtra(PushConstants.PUSH_TOKEN_INTENT_KEY)?.let {
            pushApi.registerPushToken(it)
        }
    }
}
