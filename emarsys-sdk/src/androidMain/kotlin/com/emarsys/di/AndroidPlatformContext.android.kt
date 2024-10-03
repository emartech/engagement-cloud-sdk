package com.emarsys.di

import com.emarsys.core.actions.ActionHandlerApi
import com.emarsys.core.channel.CustomEventChannelApi
import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.push.NotificationIntentProcessor
import kotlinx.serialization.json.Json

class AndroidPlatformContext(
    private val json: Json,
    private val actionFactory: ActionFactoryApi<ActionModel>,
    private val actionHandler: ActionHandlerApi,
    private val eventChannel: CustomEventChannelApi
) : PlatformContext {

    val notificationIntentProcessor: NotificationIntentProcessor by lazy {
        NotificationIntentProcessor(json, actionFactory, actionHandler, eventChannel)
    }
}