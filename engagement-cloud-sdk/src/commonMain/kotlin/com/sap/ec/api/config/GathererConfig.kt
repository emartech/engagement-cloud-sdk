package com.sap.ec.api.config

import com.sap.ec.core.log.Logger

internal class GathererConfig(
    val configContext: ConfigContextApi,
    private val sdkLogger: Logger
) : ConfigInstance {
    override suspend fun changeApplicationCode(applicationCode: String) {
        sdkLogger.debug("GathererConfig - changeApplicationCode")
        configContext.calls.add(ConfigCall.ChangeApplicationCode(applicationCode))
    }

    override suspend fun setLanguage(language: String) {
        sdkLogger.debug("GathererConfig - setLanguage")
        configContext.calls.add(ConfigCall.SetLanguage(language))
    }

    override suspend fun resetLanguage() {
        sdkLogger.debug("GathererConfig - resetLanguage")
        configContext.calls.add(ConfigCall.ResetLanguage)
    }

    override suspend fun activate() {}
}