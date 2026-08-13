package com.sap.ec.api.config

import kotlinx.serialization.Serializable

@Serializable
internal sealed interface ConfigCall {
    @Serializable
    data class ChangeApplicationCode(val applicationCode: String) : ConfigCall

    @Serializable
    data class SetLanguage(val language: String) : ConfigCall

    @Serializable
    object ResetLanguage : ConfigCall
}