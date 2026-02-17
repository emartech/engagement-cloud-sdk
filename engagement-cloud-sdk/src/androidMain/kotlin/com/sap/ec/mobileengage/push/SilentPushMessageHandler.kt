package com.sap.ec.mobileengage.push

import com.sap.ec.SdkConstants.SILENT_PUSH_RECEIVED_EVENT_NAME
import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.event.SdkEvent
import com.sap.ec.mobileengage.action.PushActionFactoryApi
import com.sap.ec.mobileengage.push.model.AndroidPlatformData
import com.sap.ec.mobileengage.push.model.SilentAndroidPushMessage
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
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