package com.emarsys.api.config

import com.emarsys.api.generic.ApiContext

class GathererConfig(val context: ApiContext<ConfigCall>): ConfigInstance {
    override suspend fun changeApplicationCode(applicationCode: String) {
        context.calls.add(ConfigCall.ChangeApplicationCode(applicationCode))
    }

    override suspend fun changeMerchantId(merchantId: String) {
        context.calls.add(ConfigCall.ChangeMerchantId(merchantId))
    }

    override suspend fun activate() {}
}