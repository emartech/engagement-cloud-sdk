package com.emarsys.api.config

import com.emarsys.api.SdkResult
import com.emarsys.core.device.NotificationSettings

class Config:ConfigApi {
    override val contactFieldId: Int?
        get() = TODO("Not yet implemented")
    override val applicationCode: String?
        get() = TODO("Not yet implemented")
    override val merchantId: String?
        get() = TODO("Not yet implemented")
    override val hardwareId: String
        get() = TODO("Not yet implemented")
    override val languageCode: String
        get() = TODO("Not yet implemented")
    override val notificationSettings: NotificationSettings
        get() = TODO("Not yet implemented")
    override val isAutomaticPushSendingEnabled: Boolean
        get() = TODO("Not yet implemented")
    override val sdkVersion: String
        get() = TODO("Not yet implemented")

    override fun changeApplicationCode(applicationCode: String?): SdkResult {
        TODO("Not yet implemented")
    }

    override fun changeMerchantId(merchantId: String?): SdkResult {
        TODO("Not yet implemented")
    }
}