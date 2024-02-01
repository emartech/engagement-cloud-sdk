package com.emarsys.action

interface OnEventActionFactoryApi {
    suspend fun create(action: ActionModel): ExecutableCommand
}