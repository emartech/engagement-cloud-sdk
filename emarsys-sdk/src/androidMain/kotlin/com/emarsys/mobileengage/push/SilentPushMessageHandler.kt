package com.emarsys.mobileengage.push

import com.emarsys.SdkConstants.PUSH_RECEIVED_EVENT_NAME
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.mobileengage.action.PushActionFactoryApi
import com.emarsys.mobileengage.push.model.AndroidPlatformData
import com.emarsys.mobileengage.push.model.SilentAndroidPushMessage

import com.emarsys.networking.clients.event.model.SdkEvent
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

internal class SilentPushMessageHandler(
    private val pushActionFactory: PushActionFactoryApi,
    private val sdkEventDistributor: SdkEventDistributorApi
): PushHandler<AndroidPlatformData, SilentAndroidPushMessage> {

    override suspend fun handle(pushMessage: SilentAndroidPushMessage) {
        pushMessage.actionableData?.actions?.forEach { action ->
            pushActionFactory.create(action).invoke()
        }

        sdkEventDistributor.registerAndStoreEvent(
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