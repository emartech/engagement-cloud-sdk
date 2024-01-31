package com.emarsys.action

typealias Command = suspend () -> Unit

interface ActionCommandFactoryApi {
    suspend fun create(action: ActionModel): Command
}