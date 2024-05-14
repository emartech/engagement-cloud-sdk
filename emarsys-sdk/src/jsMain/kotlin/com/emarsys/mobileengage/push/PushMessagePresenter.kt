package com.emarsys.mobileengage.push

import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.models.ActionModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import web.serviceworker.NotificationEvent
import web.window.window

open class PushMessagePresenter(
    private val pushServiceContext: PushServiceContext,
    private val actionFactory: ActionFactoryApi<ActionModel>,
    private val sdkDispatcher: CoroutineDispatcher
): PushPresenter {

    override suspend fun present(pushMessage: PushMessage) {
        // TODO: show message
        pushServiceContext.registration.showNotification(
            pushMessage.title
        )

        window.addEventListener(NotificationEvent.NOTIFICATION_CLICK, { event ->
            CoroutineScope(sdkDispatcher).launch {
                val actionModel = pushMessage.data?.actions?.first { it.type == event.action }
                actionModel?.let {
                    val action = actionFactory.create(it)
                    action()
                }
            }
        })
    }

}