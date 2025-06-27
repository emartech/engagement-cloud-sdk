package com.emarsys.mobileengage.push

import com.emarsys.SdkConstants.SILENT_PUSH_RECEIVED_EVENT_NAME
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.event.SdkEvent
import com.emarsys.mobileengage.action.PushActionFactoryApi
import com.emarsys.mobileengage.push.model.AndroidPlatformData
import com.emarsys.mobileengage.push.model.SilentAndroidPushMessage

internal class SilentPushMessageHandler(
    private val pushActionFactory: PushActionFactoryApi,
    private val sdkEventDistributor: SdkEventDistributorApi
) : PushHandler<AndroidPlatformData, SilentAndroidPushMessage> {

    override suspend fun handle(pushMessage: SilentAndroidPushMessage) {
        pushMessage.actionableData?.actions?.forEach { action ->
            pushActionFactory.create(action).invoke()
        }

        sdkEventDistributor.registerEvent(
            SdkEvent.External.Api.AppEvent(
                name = SILENT_PUSH_RECEIVED_EVENT_NAME
            )
        )
    }
}