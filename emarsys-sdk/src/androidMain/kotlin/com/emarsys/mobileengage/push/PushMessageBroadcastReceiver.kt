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
import com.emarsys.mobileengage.push.model.AndroidPushMessage
import com.emarsys.mobileengage.push.model.SilentAndroidPushMessage
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

class PushMessageBroadcastReceiver : BroadcastReceiver() {
    private val dependencyContainer = DependencyInjection.container as DependencyContainerPrivateApi
    private val pushPresenter =
        (dependencyContainer.platformContext as AndroidPlatformContext).pushMessagePresenter
    private val silentPushHandler =
        (dependencyContainer.platformContext as AndroidPlatformContext).silentPushHandler
    private val pushMessageFactory =
        (dependencyContainer.platformContext as AndroidPlatformContext).androidPushMessageFactory
    private val sdkDispatcher = dependencyContainer.sdkDispatcher
    private val logger: Logger = dependencyContainer.sdkLogger
    private val json: Json = dependencyContainer.json

    override fun onReceive(context: Context, intent: Intent) = goAsync(sdkDispatcher) {
        intent.getStringExtra(PushConstants.PUSH_MESSAGE_PAYLOAD_INTENT_KEY)?.let {
            logger.debug("PushMessageBroadcastReceiver - onReceive")
            try {
                val pushPayload = json.decodeFromString<JsonObject>(it)
                logger.debug("PushMessageBroadcastReceiver - onReceive", "parsed successfully")
                pushMessageFactory.create(pushPayload)?.let { pushMessage ->
                    when (pushMessage) {
                        is SilentAndroidPushMessage -> {
                            logger.debug(
                                "PushMessageBroadcastReceiver - onReceive",
                                mapOf("type" to "silent", "message" to pushMessage)
                            )
                            silentPushHandler.handle(pushMessage)
                        }

                        is AndroidPushMessage -> {
                            logger.debug(
                                "PushMessageBroadcastReceiver - onReceive",
                                mapOf("type" to "notification", "message" to pushMessage)
                            )
                            pushPresenter.present(pushMessage)
                        }
                    }
                }
            } catch (exception: Exception) {
                logger.error("PushMessageBroadcastReceiver", exception)
            }
        }
    }
}
