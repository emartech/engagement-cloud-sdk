package com.sap.ec.core.extension

import android.content.BroadcastReceiver
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


internal fun BroadcastReceiver.goAsync(
    context: CoroutineContext,
    lambda: suspend CoroutineScope.() -> Unit
) {
    val pendingResult = goAsync()
    @OptIn(DelicateCoroutinesApi::class)
    GlobalScope.launch(context) {
        try {
            lambda()
        } catch (e: Exception) {
            Log.e("BroadcastReceiverExt", "goAsync lambda failed in ${this@goAsync::class.simpleName}: ${e.message}", e)
        } finally {
            pendingResult?.finish()
        }
    }
}