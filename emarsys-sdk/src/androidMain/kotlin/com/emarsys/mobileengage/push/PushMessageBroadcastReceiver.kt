package com.emarsys.mobileengage.push

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.emarsys.api.push.PushConstants
import com.emarsys.core.extension.goAsync
import com.emarsys.core.log.Logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.json.Json


class PushMessageBroadcastReceiver(
    private val pushPresenter: PushPresenter,
    private val sdkDispatcher: CoroutineDispatcher,
    private val logger: Logger,
    private val json: Json
) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = goAsync(sdkDispatcher) {
        intent.getStringExtra(PushConstants.PUSH_MESSAGE_PAYLOAD_INTENT_KEY)?.let {
            try {
                val pushMessage = json.decodeFromString<PushMessage>(it)
                pushPresenter.present(pushMessage)
            } catch (exception: Exception) {
                logger.error("PushMessageBroadcastReceiver", exception)
            }
        }
    }
}