package com.emarsys.mobileengage.action

interface OnEventActionFactoryApi {
    suspend fun create(action: ActionModel): ExecutableCommand
}