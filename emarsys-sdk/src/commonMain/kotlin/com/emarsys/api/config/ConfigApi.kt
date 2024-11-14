package com.emarsys.api.config

import com.emarsys.core.device.PushSettings

interface ConfigApi {

    val contactFieldId: Int?

    val applicationCode: String?

    val merchantId: String?

    val clientId: String

    val languageCode: String

    val sdkVersion: String

    suspend fun changeApplicationCode(applicationCode: String): Result<Unit>

    suspend fun changeMerchantId(merchantId: String): Result<Unit>

    suspend fun getPushSettings(): PushSettings
}