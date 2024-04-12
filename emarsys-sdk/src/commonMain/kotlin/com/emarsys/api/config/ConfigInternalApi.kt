package com.emarsys.api.config

import com.emarsys.api.SdkResult

interface ConfigInternalApi {
    suspend fun changeApplicationCode(applicationCode: String): SdkResult

    suspend fun changeMerchantId(merchantId: String): SdkResult
}