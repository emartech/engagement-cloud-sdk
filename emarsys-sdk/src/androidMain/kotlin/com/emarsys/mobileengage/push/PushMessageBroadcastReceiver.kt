package com.emarsys.mobileengage.push

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.emarsys.api.push.PushConstants
import com.emarsys.core.extension.goAsync
import com.emarsys.core.log.Logger
import com.emarsys.mobileengage.push.model.AndroidPlatformData
import com.emarsys.mobileengage.push.model.AndroidPush
import com.emarsys.mobileengage.push.model.AndroidPushMessage
import com.emarsys.mobileengage.push.model.AndroidSilentPushMessage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.json.Json


class PushMessageBroadcastReceiver(
    private val pushPresenter: PushPresenter<AndroidPlatformData, AndroidPushMessage>,
    private val silentPushHandler: PushHandler<AndroidPlatformData, AndroidSilentPushMessage>,
    private val sdkDispatcher: CoroutineDispatcher,
    private val logger: Logger,
    private val json: Json
) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = goAsync(sdkDispatcher) {
        intent.getStringExtra(PushConstants.PUSH_MESSAGE_PAYLOAD_INTENT_KEY)?.let {
            try {
                when (val parsedMessage = json.decodeFromString<AndroidPush>(it)) {
                    is AndroidPushMessage -> pushPresenter.present(parsedMessage)
                    is AndroidSilentPushMessage -> silentPushHandler.handle(parsedMessage)
                }
            } catch (exception: Exception) {
                logger.error("PushMessageBroadcastReceiver", exception)
            }
        }
    }
}