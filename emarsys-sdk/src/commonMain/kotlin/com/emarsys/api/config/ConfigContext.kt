package com.emarsys.api.config

import com.emarsys.di.SdkComponent
import kotlinx.serialization.Serializable

internal class ConfigContext(override val calls: MutableList<ConfigCall>) : ConfigContextApi, SdkComponent

@Serializable
sealed interface ConfigCall {
    @Serializable
    data class ChangeApplicationCode(val applicationCode: String) : ConfigCall

    @Serializable
    data class ChangeMerchantId(val merchantId: String) : ConfigCall

    @Serializable
    data class SetLanguage(val language: String) : ConfigCall

    @Serializable
    object ResetLanguage : ConfigCall
}