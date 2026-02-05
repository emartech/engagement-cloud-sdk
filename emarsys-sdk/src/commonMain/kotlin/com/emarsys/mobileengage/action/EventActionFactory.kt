package com.emarsys.mobileengage.action

import com.emarsys.core.actions.clipboard.ClipboardHandlerApi
import com.emarsys.core.channel.SdkEventDistributorApi
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
import com.emarsys.mobileengage.action.actions.RichContentDisplayAction
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.action.models.AppEventActionModel
import com.emarsys.mobileengage.action.models.BasicRichContentDisplayActionModel
import com.emarsys.mobileengage.action.models.CopyToClipboardActionModel
import com.emarsys.mobileengage.action.models.CustomEventActionModel
import com.emarsys.mobileengage.action.models.DismissActionModel
import com.emarsys.mobileengage.action.models.OpenExternalUrlActionModel
import com.emarsys.mobileengage.action.models.ReportingActionModel
import com.emarsys.mobileengage.action.models.RequestPushPermissionActionModel

internal class EventActionFactory(
    private val sdkEventDistributor: SdkEventDistributorApi,
    private val permissionHandler: PermissionHandlerApi,
    private val externalUrlOpener: ExternalUrlOpenerApi,
    private val clipboardHandler: ClipboardHandlerApi,
    private val sdkLogger: Logger
) : EventActionFactoryApi {
    override suspend fun create(actionModel: ActionModel): Action<*> {
        return when (actionModel) {
            is AppEventActionModel -> AppEventAction(actionModel, sdkEventDistributor)
            is CustomEventActionModel -> CustomEventAction(actionModel, sdkEventDistributor)
            is RequestPushPermissionActionModel -> RequestPushPermissionAction(permissionHandler)
            is DismissActionModel -> DismissAction(actionModel, sdkEventDistributor)
            is OpenExternalUrlActionModel -> OpenExternalUrlAction(actionModel, externalUrlOpener)
            is ReportingActionModel -> ReportingAction(actionModel, sdkEventDistributor)
            is CopyToClipboardActionModel -> CopyToClipboardAction(actionModel, clipboardHandler)
            is BasicRichContentDisplayActionModel -> RichContentDisplayAction(actionModel)
            else -> {
                val exception = IllegalArgumentException("Unknown action type: $actionModel")
                sdkLogger.error("EventActionFactory", exception)
                throw exception

            }
        }
    }

}
