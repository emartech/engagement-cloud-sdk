package com.emarsys.mobileengage.action


expect class ActionCommandFactory : ActionCommandFactoryApi {

    override suspend fun create(action: ActionModel): Command


}
