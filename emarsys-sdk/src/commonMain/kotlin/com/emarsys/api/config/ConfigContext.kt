package com.emarsys.api.config

import com.emarsys.api.generic.ApiContext
import kotlinx.serialization.Serializable

class ConfigContext(override val calls: MutableList<ConfigCall>) : ApiContext<ConfigCall>

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