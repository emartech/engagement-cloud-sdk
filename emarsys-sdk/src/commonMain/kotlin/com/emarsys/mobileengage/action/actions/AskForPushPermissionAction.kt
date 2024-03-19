package com.emarsys.mobileengage.action.actions

import com.emarsys.core.permission.PermissionHandlerApi
import com.emarsys.mobileengage.action.models.AskForPushPermissionActionModel

class AskForPushPermissionAction(
    private val action: AskForPushPermissionActionModel,
    private val permissionHandler: PermissionHandlerApi
): Action<Unit> {
    override suspend fun invoke(value: Unit?) {
        permissionHandler.requestPushPermission()
    }
}
