package com.emarsys.mobileengage.action

import com.emarsys.api.AppEvent
import com.emarsys.applicationContext

actual class ActionCommandFactory : ActionCommandFactoryApi {
    actual override suspend fun create(action: ActionModel): Command {
        return when (action) {
            is AppEventActionModel -> AppEventCommand { events ->
                events.emit(AppEvent(applicationContext, action.name, action.payload))
            }

            is CustomEventActionModel -> CustomEventCommand { }

            is DismissActionModel -> DismissCommand { }

            is OpenExternalUrlActionModel -> OpenExternalUrlCommand { }

            is BadgeCountActionModel -> BadgeCountCommand {}

            is AskForPushPermissionActionModel -> AskForPushPermissionCommand { }
        }
    }
}