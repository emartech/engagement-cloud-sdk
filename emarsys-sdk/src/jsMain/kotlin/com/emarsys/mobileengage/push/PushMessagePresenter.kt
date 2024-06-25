package com.emarsys.mobileengage.push

import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.push.model.JsPlatformData
import com.emarsys.mobileengage.push.model.JsPushMessage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import web.serviceworker.NotificationEvent
import web.window.window

open class PushMessagePresenter(
    private val pushServiceContext: PushServiceContext,
    private val actionFactory: ActionFactoryApi<ActionModel>,
    private val sdkDispatcher: CoroutineDispatcher
) : PushPresenter<JsPlatformData, JsPushMessage> {

    override suspend fun present(pushMessage: JsPushMessage) {
        // TODO: show message
        pushServiceContext.registration.showNotification(
            pushMessage.title
            // TODO add action buttons
        )

        window.addEventListener(NotificationEvent.NOTIFICATION_CLICK, { event ->
            CoroutineScope(sdkDispatcher).launch {
                val actionModel = pushMessage.data?.actions?.first { it.id == event.action }
                actionModel?.let {
                    val action = actionFactory.create(it)
                    action()
                }
            }
        })
    }

}