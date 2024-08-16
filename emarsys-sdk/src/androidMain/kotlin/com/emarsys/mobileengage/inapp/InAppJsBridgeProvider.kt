package com.emarsys.mobileengage.inapp

import com.emarsys.core.providers.Provider
import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.models.ActionModel

class InAppJsBridgeProvider(private val actionFactory: ActionFactoryApi<ActionModel>) : Provider<InAppJsBridge> {
    override fun provide(): InAppJsBridge {
        return InAppJsBridge(actionFactory)
    }
}
