package com.emarsys.mobileengage.inapp

import com.emarsys.api.inapp.InAppInternalApi
import com.emarsys.mobileengage.action.*
import com.emarsys.mobileengage.action.ExecutableCommand

class InAppActionFactory(
    private val actionCommandFactory: ActionCommandFactoryApi,
    private val inAppInternal: InAppInternalApi
) : InAppActionFactoryApi {
    override suspend fun create(action: Action): ExecutableCommand {
        if (action is InAppAction) {
            return when (action) {
                is AppEventActionModel -> ExecutableCommand {
                    (actionCommandFactory.create(action) as AppEventCommand).invoke(inAppInternal.events)
                }

                is CustomEventActionModel -> ExecutableCommand {
                    (actionCommandFactory.create(action) as CustomEventCommand).invoke()
                }

                is OpenExternalUrlActionModel -> ExecutableCommand {
                    (actionCommandFactory.create(action) as OpenExternalUrlCommand).invoke()
                }

                is DismissActionModel -> ExecutableCommand {
                    (actionCommandFactory.create(action) as DismissCommand).invoke()
                }

                is AskForPushPermissionActionModel -> ExecutableCommand {
                    (actionCommandFactory.create(action) as AskForPushPermissionCommand).invoke()
                }
            }

        } else {
            throw IllegalArgumentException("Action is not an InAppAction")
        }
    }
}