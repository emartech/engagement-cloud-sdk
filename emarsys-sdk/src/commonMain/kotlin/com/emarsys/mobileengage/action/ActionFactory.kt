package com.emarsys.mobileengage.action

import com.emarsys.api.oneventaction.OnEventActionInternalApi
import com.emarsys.core.badge.BadgeCountHandlerApi
import com.emarsys.core.channel.DeviceEventChannelApi
import com.emarsys.core.message.MsgHubApi
import com.emarsys.core.permission.PermissionHandlerApi
import com.emarsys.core.url.ExternalUrlOpenerApi
import com.emarsys.mobileengage.action.actions.Action
import com.emarsys.mobileengage.action.actions.AppEventAction
import com.emarsys.mobileengage.action.actions.AskForPushPermissionAction
import com.emarsys.mobileengage.action.actions.BadgeCountAction
import com.emarsys.mobileengage.action.actions.CustomEventAction
import com.emarsys.mobileengage.action.actions.DismissAction
import com.emarsys.mobileengage.action.actions.OpenExternalUrlAction
import com.emarsys.mobileengage.action.models.AppEventActionModel
import com.emarsys.mobileengage.action.models.AskForPushPermissionActionModel
import com.emarsys.mobileengage.action.models.BadgeCountActionModel
import com.emarsys.mobileengage.action.models.CustomEventActionModel
import com.emarsys.mobileengage.action.models.DismissActionModel
import com.emarsys.mobileengage.action.models.OpenExternalUrlActionModel

class ActionFactory<ActionModelType>(
    private val onEventActionInternal: OnEventActionInternalApi,
    private val eventChannel: DeviceEventChannelApi,
    private val permissionHandler: PermissionHandlerApi,
    private val badgeCountHandler: BadgeCountHandlerApi,
    private val externalUrlOpener: ExternalUrlOpenerApi,
    private val msgHub: MsgHubApi
): ActionFactoryApi<ActionModelType> {

    override suspend fun create(action: ActionModelType): Action<*> {
        return when (action) {
            is AppEventActionModel -> AppEventAction(action, onEventActionInternal)
            is CustomEventActionModel -> CustomEventAction(action, eventChannel)
            is AskForPushPermissionActionModel -> AskForPushPermissionAction(action, permissionHandler)
            is BadgeCountActionModel -> BadgeCountAction(action, badgeCountHandler)
            is DismissActionModel -> DismissAction(action, msgHub)
            is OpenExternalUrlActionModel -> OpenExternalUrlAction(action, externalUrlOpener)
            else -> throw IllegalArgumentException("Unknown action type: $action")
        }
    }

}
