package com.sap.ec.api.config

import com.sap.ec.core.collections.ThreadSafePersistentStoreApi
import com.sap.ec.core.log.Logger

internal class GathererConfig(
    private val threadSafePersistentStore: ThreadSafePersistentStoreApi<ConfigCall>,
    private val sdkLogger: Logger
) : ConfigInstance {
    override suspend fun changeApplicationCode(applicationCode: String) {
        sdkLogger.debug("GathererConfig - changeApplicationCode")
        threadSafePersistentStore.add(ConfigCall.ChangeApplicationCode(applicationCode))
    }

    override suspend fun setLanguage(language: String) {
        sdkLogger.debug("GathererConfig - setLanguage")
        threadSafePersistentStore.add(ConfigCall.SetLanguage(language))
    }

    override suspend fun resetLanguage() {
        sdkLogger.debug("GathererConfig - resetLanguage")
        threadSafePersistentStore.add(ConfigCall.ResetLanguage)
    }

    override suspend fun activate() {}
}