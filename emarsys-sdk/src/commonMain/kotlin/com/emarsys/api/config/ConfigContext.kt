package com.emarsys.api.config

import kotlinx.serialization.Serializable

internal class ConfigContext(override val calls: MutableList<ConfigCall>) : ConfigContextApi

@Serializable
sealed interface ConfigCall {
    @Serializable
    data class ChangeApplicationCode(val applicationCode: String) : ConfigCall

    @Serializable
    data class SetLanguage(val language: String) : ConfigCall

    @Serializable
    object ResetLanguage : ConfigCall
}