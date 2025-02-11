package com.emarsys.mobileengage.push

import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.push.model.AndroidPlatformData
import com.emarsys.mobileengage.push.model.AndroidSilentPushMessage

class SilentPushMessageHandler(
    private val pushActionFactory: ActionFactoryApi<ActionModel>,
) : PushHandler<AndroidPlatformData, AndroidSilentPushMessage> {

    override suspend fun handle(pushMessage: AndroidSilentPushMessage) {
        pushMessage.data.actions?.forEach { action ->
            pushActionFactory.create(action).invoke()
        }
    }
}