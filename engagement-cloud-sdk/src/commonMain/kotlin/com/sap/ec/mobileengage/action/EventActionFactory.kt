package com.sap.ec.mobileengage.action

import com.sap.ec.core.actions.clipboard.ClipboardHandlerApi
import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.permission.PermissionHandlerApi
import com.sap.ec.core.url.ExternalUrlOpenerApi
import com.sap.ec.mobileengage.action.actions.Action
import com.sap.ec.mobileengage.action.actions.AppEventAction
import com.sap.ec.mobileengage.action.actions.CopyToClipboardAction
import com.sap.ec.mobileengage.action.actions.CustomEventAction
import com.sap.ec.mobileengage.action.actions.DismissAction
import com.sap.ec.mobileengage.action.actions.OpenExternalUrlAction
import com.sap.ec.mobileengage.action.actions.ReportingAction
import com.sap.ec.mobileengage.action.actions.RequestPushPermissionAction
import com.sap.ec.mobileengage.action.actions.RichContentDisplayAction
import com.sap.ec.mobileengage.action.models.ActionModel
import com.sap.ec.mobileengage.action.models.AppEventActionModel
import com.sap.ec.mobileengage.action.models.BasicRichContentDisplayActionModel
import com.sap.ec.mobileengage.action.models.CopyToClipboardActionModel
import com.sap.ec.mobileengage.action.models.CustomEventActionModel
import com.sap.ec.mobileengage.action.models.DismissActionModel
import com.sap.ec.mobileengage.action.models.OpenExternalUrlActionModel
import com.sap.ec.mobileengage.action.models.ReportingActionModel
import com.sap.ec.mobileengage.action.models.RequestPushPermissionActionModel

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
