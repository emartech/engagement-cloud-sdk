package com.emarsys.api.config

import com.emarsys.core.log.Logger

class ConfigInternal(val sdkLogger: Logger) : ConfigInstance {

    override suspend fun changeApplicationCode(applicationCode: String) {
        sdkLogger.debug("ConfigInternal - changeApplicationCode")
        TODO("Not yet implemented")
    }

    override suspend fun changeMerchantId(merchantId: String) {
        sdkLogger.debug("ConfigInternal - changeMerchantId")
        TODO("Not yet implemented")
    }

    override suspend fun activate() {
        sdkLogger.debug("ConfigInternal - activate")
    }
}