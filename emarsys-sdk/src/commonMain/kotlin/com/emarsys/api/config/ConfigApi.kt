package com.emarsys.api.config

import com.emarsys.api.SdkResult
import com.emarsys.core.device.NotificationSettings

interface ConfigApi {

    val contactFieldId: Int?

    val applicationCode: String?

    val merchantId: String?

    val hardwareId: String

    val languageCode: String

    val notificationSettings: NotificationSettings

    val isAutomaticPushSendingEnabled: Boolean

    val sdkVersion: String

    fun changeApplicationCode(applicationCode: String?): SdkResult


    fun changeMerchantId(merchantId: String?): SdkResult
}