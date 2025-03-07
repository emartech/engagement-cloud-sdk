package com.emarsys.api.config

import com.emarsys.api.generic.ApiContext
import com.emarsys.core.log.Logger

class GathererConfig(
    val context: ApiContext<ConfigCall>,
    private val sdkLogger: Logger
) :
    ConfigInstance {
    override suspend fun changeApplicationCode(applicationCode: String) {
        sdkLogger.debug("GathererConfig - changeApplicationCode")
        context.calls.add(ConfigCall.ChangeApplicationCode(applicationCode))
    }

    override suspend fun changeMerchantId(merchantId: String) {
        sdkLogger.debug("GathererConfig - changeMerchantId")
        context.calls.add(ConfigCall.ChangeMerchantId(merchantId))
    }

    override suspend fun activate() {}
}