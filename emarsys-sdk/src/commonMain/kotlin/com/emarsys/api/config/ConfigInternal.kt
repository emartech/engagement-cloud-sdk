package com.emarsys.api.config

import com.emarsys.core.log.Logger
import com.emarsys.core.providers.Provider
import com.emarsys.networking.clients.event.model.SdkEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

class ConfigInternal(
    private val sdkEventFlow: MutableSharedFlow<SdkEvent>,
    private val uuidProvider: Provider<String>,
    private val timestampProvider: Provider<Instant>,
    private val sdkLogger: Logger
) :
    ConfigInstance {

    override suspend fun changeApplicationCode(applicationCode: String) {
        sdkLogger.debug("ConfigInternal - changeApplicationCode")
        sdkEventFlow.emit(
            SdkEvent.Internal.Sdk.ChangeAppCode(
                uuidProvider.provide(),
                buildJsonObject {
                    put(
                        "applicationCode",
                        JsonPrimitive(applicationCode)
                    )
                },
                timestampProvider.provide()
            )
        )
    }

    override suspend fun changeMerchantId(merchantId: String) {
        sdkLogger.debug("ConfigInternal - changeMerchantId")
        TODO("Not yet implemented")
    }

    override suspend fun activate() {
        sdkLogger.debug("ConfigInternal - activate")
    }
}