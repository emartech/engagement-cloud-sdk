package com.emarsys.mobileengage.action

import com.emarsys.api.oneventaction.OnEventActionInternalApi


class OnEventActionFactory(
    private val actionCommandFactory: ActionCommandFactoryApi,
    private val onEventActionInternal: OnEventActionInternalApi
) : OnEventActionFactoryApi {

    override suspend fun create(action: ActionModel): ExecutableCommand {
        if (action is OnEventAction) {
            return when (action) {
                is AppEventActionModel -> ExecutableCommand {
                    (actionCommandFactory.create(action) as AppEventCommand).invoke(onEventActionInternal.events)
                }


                is CustomEventActionModel -> ExecutableCommand { }

            }

        } else {
            throw IllegalArgumentException("Action is not an OnEventAction")
        }
    }
}
