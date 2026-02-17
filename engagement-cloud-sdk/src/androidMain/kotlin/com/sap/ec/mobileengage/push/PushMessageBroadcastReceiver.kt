package com.sap.ec.mobileengage.push

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sap.ec.api.push.PushConstants
import com.sap.ec.core.extension.goAsync
import com.sap.ec.core.log.Logger
import com.sap.ec.di.DispatcherTypes
import com.sap.ec.di.SdkComponent
import com.sap.ec.mobileengage.push.model.AndroidPushMessage
import com.sap.ec.mobileengage.push.model.SilentAndroidPushMessage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named

internal class PushMessageBroadcastReceiver : BroadcastReceiver(), SdkComponent {
    private val sdkDispatcher: CoroutineDispatcher by inject(named(DispatcherTypes.Sdk))
    private val logger: Logger by inject() { parametersOf(PushMessageBroadcastReceiver::class.simpleName) }
    private val json: Json by inject()
    private val pushMessagePresenter: PushMessagePresenter by inject()
    private val silentPushMessageHandler: SilentPushMessageHandler by inject()
    private val pushMessageFactory: AndroidPushMessageFactory by inject()

    override fun onReceive(context: Context, intent: Intent) = goAsync(sdkDispatcher) {
        intent.getStringExtra(PushConstants.PUSH_MESSAGE_PAYLOAD_INTENT_KEY)?.let {
            logger.debug("push message received")
            try {
                val pushPayload = json.decodeFromString<JsonObject>(it)
                logger.debug("parsed successfully")
                pushMessageFactory.create(pushPayload)?.let { pushMessage ->
                    when (pushMessage) {
                        is SilentAndroidPushMessage -> {
                            logger.debug(
                                "handling Silent message",
                                buildJsonObject {
                                    put("type", JsonPrimitive("silent"))
                                    put("message", JsonPrimitive(pushMessage.toString()))
                                })
                            silentPushMessageHandler.handle(pushMessage)
                        }

                        is AndroidPushMessage -> {
                            logger.debug(
                                "presenting Android message",
                                buildJsonObject {
                                    put("type", JsonPrimitive("notification"))
                                    put("message", JsonPrimitive(pushMessage.toString()))
                                }
                            )
                            pushMessagePresenter.present(pushMessage)
                        }
                    }
                }
            } catch (exception: Exception) {
                logger.error("push presentation failed", exception)
            }
        }
    }
}