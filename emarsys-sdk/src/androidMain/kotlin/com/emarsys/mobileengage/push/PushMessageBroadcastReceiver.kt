package com.emarsys.mobileengage.push

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.emarsys.api.push.PushConstants
import com.emarsys.core.extension.goAsync
import com.emarsys.core.log.Logger
import com.emarsys.di.AndroidPlatformContext
import com.emarsys.di.DependencyContainerPrivateApi
import com.emarsys.di.DependencyInjection
import com.emarsys.mobileengage.push.model.AndroidPush
import com.emarsys.mobileengage.push.model.AndroidPushMessage
import com.emarsys.mobileengage.push.model.AndroidSilentPushMessage
import com.emarsys.util.JsonUtil
import kotlinx.serialization.json.Json


class PushMessageBroadcastReceiver : BroadcastReceiver() {
    private val dependencyContainer = DependencyInjection.container as DependencyContainerPrivateApi
    private val pushPresenter =
        (dependencyContainer.platformContext as AndroidPlatformContext).pushMessagePresenter
    private val silentPushHandler =
        (dependencyContainer.platformContext as AndroidPlatformContext).silentPushHandler
    private val sdkDispatcher = dependencyContainer.sdkDispatcher
    private val logger: Logger = dependencyContainer.sdkLogger
    private val json: Json = JsonUtil.json

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