package com.emarsys.api.config

import com.emarsys.core.device.PushSettings

interface ConfigApi {

    val contactFieldId: Int?

    val applicationCode: String?

    val merchantId: String?

    val hardwareId: String

    val languageCode: String

    val pushSettings: PushSettings

    val sdkVersion: String

    suspend fun changeApplicationCode(applicationCode: String): Result<Unit>

    suspend fun changeMerchantId(merchantId: String): Result<Unit>
}