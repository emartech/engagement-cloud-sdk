package com.emarsys.action

import com.emarsys.api.oneventaction.OnEventActionInternalApi


class ActionCommandFactory(private val onEventActionInternal: OnEventActionInternalApi) : ActionCommandFactoryApi {

    override suspend fun create(action: ActionModel): Command {
        return when (action) {
            is AppEventActionModel -> {
                { }
            }

            is CustomEventActionModel -> {
                { }
            }

            is DismissActionModel -> {
                { }
            }

            is OpenExternalUrlActionModel -> {
                { }
            }
        }
    }
}
