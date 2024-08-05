package com.emarsys.mobileengage.action.actions

import com.emarsys.core.permission.PermissionHandlerApi
import com.emarsys.mobileengage.action.models.RequestPushPermissionActionModel

class RequestPushPermissionAction(
    private val action: RequestPushPermissionActionModel,
    private val permissionHandler: PermissionHandlerApi
): Action<Unit> {
    override suspend fun invoke(value: Unit?) {
        permissionHandler.requestPushPermission()
    }
}
