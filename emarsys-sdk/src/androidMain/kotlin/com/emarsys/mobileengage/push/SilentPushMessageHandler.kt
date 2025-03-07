package com.emarsys.mobileengage.push

import com.emarsys.SdkConstants.PUSH_RECEIVED_EVENT_NAME
import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.push.model.AndroidPlatformData
import com.emarsys.mobileengage.push.model.SilentAndroidPushMessage

import com.emarsys.networking.clients.event.model.SdkEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

class SilentPushMessageHandler(
    private val pushActionFactory: ActionFactoryApi<ActionModel>,
    private val sdkEventFlow: MutableSharedFlow<SdkEvent>
): PushHandler<AndroidPlatformData, SilentAndroidPushMessage> {

    override suspend fun handle(pushMessage: SilentAndroidPushMessage) {
        pushMessage.actionableData?.actions?.forEach { action ->
            pushActionFactory.create(action).invoke()
        }

        sdkEventFlow.emit(
            SdkEvent.External.Api.SilentPush(
                name = PUSH_RECEIVED_EVENT_NAME,
                attributes = buildJsonObject {
                    put(
                        "campaignId",
                        JsonPrimitive(pushMessage.campaignId)
                    )
                })
        )
    }
}