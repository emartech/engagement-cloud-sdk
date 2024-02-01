package com.emarsys.action

import com.emarsys.api.oneventaction.OnEventActionInternal
import com.emarsys.api.oneventaction.OnEventActionInternalApi


expect class ActionCommandFactory :ActionCommandFactoryApi {

    override suspend fun create(action: ActionModel): Command


}
