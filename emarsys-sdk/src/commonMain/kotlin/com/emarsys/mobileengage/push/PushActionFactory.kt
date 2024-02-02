package com.emarsys.mobileengage.push

import com.emarsys.api.push.PushInternalApi
import com.emarsys.mobileengage.action.*
import com.emarsys.mobileengage.action.ExecutableCommand


class PushActionFactory(
    private val actionCommandFactory: ActionCommandFactoryApi,
    private val pushInternal: PushInternalApi
) : PushActionFactoryApi {

    override suspend fun create(action: Action): ExecutableCommand {
        if (action is PushAction) {
            return when (action) {
                is AppEventActionModel -> ExecutableCommand {
                    (actionCommandFactory.create(action) as AppEventCommand).invoke(pushInternal.notificationEvents)
                }

                is CustomEventActionModel -> ExecutableCommand {
                    (actionCommandFactory.create(action) as CustomEventCommand).invoke()
                }

                is DismissActionModel -> ExecutableCommand {
                    (actionCommandFactory.create(action) as DismissCommand).invoke()
                }

                is OpenExternalUrlActionModel -> ExecutableCommand {
                    (actionCommandFactory.create(action) as OpenExternalUrlCommand).invoke()
                }

                is BadgeCountActionModel -> ExecutableCommand {
                    (actionCommandFactory.create(action) as BadgeCountCommand).invoke()
                }
            }

        } else {
            throw IllegalArgumentException("Action is not a PushAction")
        }
    }
}
