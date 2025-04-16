package com.emarsys.mobileengage.action.actions

import com.emarsys.core.permission.PermissionHandlerApi

class RequestPushPermissionAction(
    private val permissionHandler: PermissionHandlerApi
): Action<Unit> {
    override suspend fun invoke(value: Unit?) {
        permissionHandler.requestPushPermission()
    }
}
