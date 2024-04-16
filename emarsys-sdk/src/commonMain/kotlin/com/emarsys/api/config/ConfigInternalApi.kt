package com.emarsys.api.config

interface ConfigInternalApi {
    suspend fun changeApplicationCode(applicationCode: String)

    suspend fun changeMerchantId(merchantId: String)
}