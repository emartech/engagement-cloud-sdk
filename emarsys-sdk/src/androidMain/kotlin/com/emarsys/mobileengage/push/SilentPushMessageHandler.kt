package com.emarsys.mobileengage.push

import com.emarsys.SdkConstants.PUSH_RECEIVED_EVENT_NAME
import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.events.SdkEvent
import com.emarsys.mobileengage.events.SdkEventSource
import com.emarsys.mobileengage.push.model.AndroidPlatformData
import com.emarsys.mobileengage.push.model.AndroidSilentPushMessage
import kotlinx.coroutines.flow.MutableSharedFlow

class SilentPushMessageHandler(
    private val pushActionFactory: ActionFactoryApi<ActionModel>,
    private val sdkEventFlow: MutableSharedFlow<SdkEvent>
) : PushHandler<AndroidPlatformData, AndroidSilentPushMessage> {

    override suspend fun handle(pushMessage: AndroidSilentPushMessage) {
        pushMessage.data.actions?.forEach { action ->
            pushActionFactory.create(action).invoke()
        }

        sdkEventFlow.emit(
            SdkEvent(
                SdkEventSource.SilentPush,
                PUSH_RECEIVED_EVENT_NAME,
                mapOf("campaignId" to pushMessage.data.campaignId)
            )
        )

    }
}