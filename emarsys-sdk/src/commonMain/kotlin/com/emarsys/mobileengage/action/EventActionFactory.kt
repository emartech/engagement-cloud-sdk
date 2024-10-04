package com.emarsys.mobileengage.action

import com.emarsys.core.badge.BadgeCountHandlerApi
import com.emarsys.core.channel.CustomEventChannelApi
import com.emarsys.core.clipboard.ClipboardHandlerApi
import com.emarsys.core.log.SdkLogger
import com.emarsys.core.message.MsgHubApi
import com.emarsys.core.permission.PermissionHandlerApi
import com.emarsys.core.url.ExternalUrlOpenerApi
import com.emarsys.mobileengage.action.actions.Action
import com.emarsys.mobileengage.action.actions.AppEventAction
import com.emarsys.mobileengage.action.actions.BadgeCountAction
import com.emarsys.mobileengage.action.actions.ButtonClickedAction
import com.emarsys.mobileengage.action.actions.CopyToClipboardAction
import com.emarsys.mobileengage.action.actions.CustomEventAction
import com.emarsys.mobileengage.action.actions.DismissAction
import com.emarsys.mobileengage.action.actions.OpenExternalUrlAction
import com.emarsys.mobileengage.action.actions.RequestPushPermissionAction
import com.emarsys.mobileengage.action.models.AppEventActionModel
import com.emarsys.mobileengage.action.models.BadgeCountActionModel
import com.emarsys.mobileengage.action.models.ButtonClickedActionModel
import com.emarsys.mobileengage.action.models.CopyToClipboardActionModel
import com.emarsys.mobileengage.action.models.CustomEventActionModel
import com.emarsys.mobileengage.action.models.DismissActionModel
import com.emarsys.mobileengage.action.models.OpenExternalUrlActionModel
import com.emarsys.mobileengage.action.models.RequestPushPermissionActionModel
import com.emarsys.mobileengage.events.SdkEvent
import kotlinx.coroutines.flow.MutableSharedFlow

class EventActionFactory<ActionModelType>(
    private val sdkEventFlow: MutableSharedFlow<SdkEvent>,
    private val eventChannel: CustomEventChannelApi,
    private val permissionHandler: PermissionHandlerApi,
    private val badgeCountHandler: BadgeCountHandlerApi,
    private val externalUrlOpener: ExternalUrlOpenerApi,
    private val msgHub: MsgHubApi,
    private val clipboardHandler: ClipboardHandlerApi,
    private val sdkLogger: SdkLogger
) : ActionFactoryApi<ActionModelType> {
    override suspend fun create(action: ActionModelType): Action<*> {
        return when (action) {
            is AppEventActionModel -> AppEventAction(action, sdkEventFlow)
            is CustomEventActionModel -> CustomEventAction(action, eventChannel)
            is RequestPushPermissionActionModel -> RequestPushPermissionAction(
                action,
                permissionHandler
            )

            is BadgeCountActionModel -> BadgeCountAction(action, badgeCountHandler)
            is DismissActionModel -> DismissAction(action, msgHub)
            is OpenExternalUrlActionModel -> OpenExternalUrlAction(action, externalUrlOpener)
            is ButtonClickedActionModel -> ButtonClickedAction(action, eventChannel)
            is CopyToClipboardActionModel -> CopyToClipboardAction(action, clipboardHandler)
            else -> {
                val exception = IllegalArgumentException("Unknown action type: $action")
                sdkLogger.error("EventActionFactory", exception)
                throw exception

            }
        }
    }

}
