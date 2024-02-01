package com.emarsys.action

import com.emarsys.api.AppEvent
import kotlinx.coroutines.flow.MutableSharedFlow

interface Command
fun interface ExecutableCommand : Command {
    suspend operator fun invoke()
}

fun interface AppEventCommand : Command {
    suspend operator fun invoke(events: MutableSharedFlow<AppEvent>)
}

fun interface CustomEventCommand : Command {
    suspend operator fun invoke()
}

fun interface DismissCommand : Command {
    suspend operator fun invoke()
}

fun interface AskForPushPermissionCommand : Command {
    suspend operator fun invoke()
}

fun interface OpenExternalUrlCommand : Command {
    suspend operator fun invoke()
}

fun interface BadgeCountCommand : Command {
    suspend operator fun invoke()
}

interface ActionCommandFactoryApi {
    suspend fun create(action: ActionModel): Command
}