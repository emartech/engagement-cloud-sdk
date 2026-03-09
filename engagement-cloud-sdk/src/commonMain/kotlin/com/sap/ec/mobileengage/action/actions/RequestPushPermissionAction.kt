package com.sap.ec.mobileengage.action.actions

import com.sap.ec.core.permission.PermissionHandlerApi

internal class RequestPushPermissionAction(
    private val permissionHandler: PermissionHandlerApi
): Action<Unit> {
    override suspend fun invoke(value: Unit?) {
        permissionHandler.requestPushPermission()
    }
}
