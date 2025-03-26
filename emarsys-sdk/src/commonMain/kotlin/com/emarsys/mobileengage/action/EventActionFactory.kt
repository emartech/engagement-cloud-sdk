package com.emarsys.mobileengage.action

import com.emarsys.core.actions.clipboard.ClipboardHandlerApi
import com.emarsys.core.log.Logger
import com.emarsys.core.permission.PermissionHandlerApi
import com.emarsys.core.url.ExternalUrlOpenerApi
import com.emarsys.mobileengage.action.actions.Action
import com.emarsys.mobileengage.action.actions.AppEventAction
import com.emarsys.mobileengage.action.actions.CopyToClipboardAction
import com.emarsys.mobileengage.action.actions.CustomEventAction
import com.emarsys.mobileengage.action.actions.DismissAction
import com.emarsys.mobileengage.action.actions.OpenExternalUrlAction
import com.emarsys.mobileengage.action.actions.ReportingAction
import com.emarsys.mobileengage.action.actions.RequestPushPermissionAction
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.action.models.AppEventActionModel
import com.emarsys.mobileengage.action.models.CopyToClipboardActionModel
import com.emarsys.mobileengage.action.models.CustomEventActionModel
import com.emarsys.mobileengage.action.models.DismissActionModel
import com.emarsys.mobileengage.action.models.OpenExternalUrlActionModel
import com.emarsys.mobileengage.action.models.ReportingActionModel
import com.emarsys.mobileengage.action.models.RequestPushPermissionActionModel

import com.emarsys.networking.clients.event.model.SdkEvent
import kotlinx.coroutines.flow.MutableSharedFlow

internal class EventActionFactory(
    private val sdkEventFlow: MutableSharedFlow<SdkEvent>,
    private val permissionHandler: PermissionHandlerApi,
    private val externalUrlOpener: ExternalUrlOpenerApi,
    private val clipboardHandler: ClipboardHandlerApi,
    private val sdkLogger: Logger
) : EventActionFactoryApi {
    override suspend fun create(action: ActionModel): Action<*> {
        return when (action) {
            is AppEventActionModel -> AppEventAction(action, sdkEventFlow)
            is CustomEventActionModel -> CustomEventAction(action, sdkEventFlow)
            is RequestPushPermissionActionModel -> RequestPushPermissionAction(
                action,
                permissionHandler
            )

            is DismissActionModel -> DismissAction(action, sdkEventFlow)
            is OpenExternalUrlActionModel -> OpenExternalUrlAction(action, externalUrlOpener)
            is ReportingActionModel -> ReportingAction(action, sdkEventFlow)
            is CopyToClipboardActionModel -> CopyToClipboardAction(action, clipboardHandler)
            else -> {
                val exception = IllegalArgumentException("Unknown action type: $action")
                sdkLogger.error("EventActionFactory", exception)
                throw exception

            }
        }
    }

}
