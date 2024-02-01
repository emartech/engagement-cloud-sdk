package com.emarsys.action

import com.emarsys.api.AppEvent
import com.emarsys.api.oneventaction.OnEventActionInternalApi

actual class ActionCommandFactory() :
    ActionCommandFactoryApi {
    actual override suspend fun create(action: ActionModel): Command {
        return when (action) {
            is AppEventActionModel -> AppEventCommand { events ->
                events.emit(AppEvent(action.name, action.payload))
            }

            is CustomEventActionModel -> {
                ExecutableCommand { }
            }

            is DismissActionModel -> {
                ExecutableCommand { }
            }

            is OpenExternalUrlActionModel -> {
                ExecutableCommand { }
            }
        }
    }
}