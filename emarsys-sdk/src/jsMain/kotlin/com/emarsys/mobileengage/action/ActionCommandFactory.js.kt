package com.emarsys.mobileengage.action

import com.emarsys.api.AppEvent

actual class ActionCommandFactory() :
    ActionCommandFactoryApi {
    actual override suspend fun create(action: ActionModel): Command {
        return when (action) {
            is AppEventActionModel -> AppEventCommand { events ->
                events.emit(AppEvent(action.name, action.payload))
            }

            is CustomEventActionModel -> ExecutableCommand { }

            is DismissActionModel -> ExecutableCommand { }

            is OpenExternalUrlActionModel -> ExecutableCommand { }

            is BadgeCountActionModel -> BadgeCountCommand {}

            is AskForPushPermissionActionModel -> AskForPushPermissionCommand { }
        }
    }
}