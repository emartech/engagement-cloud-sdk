package com.emarsys.push

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.emarsys.api.push.PushApi
import com.emarsys.api.push.PushConstants
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

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

fun BroadcastReceiver.goAsync(
    context: CoroutineContext,
    lambda: suspend CoroutineScope.() -> Unit
) {
    val pendingResult = goAsync()
    @OptIn(DelicateCoroutinesApi::class)
    GlobalScope.launch(context) {
        try {
            lambda()
        } finally {
            pendingResult.finish()
        }
    }
}