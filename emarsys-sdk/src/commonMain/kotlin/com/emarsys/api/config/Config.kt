package com.emarsys.api.config

import Activatable
import com.emarsys.api.generic.GenericApi
import com.emarsys.context.SdkContextApi
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.device.DeviceInfoCollectorApi
import com.emarsys.core.device.PushSettings
import com.emarsys.core.log.withLogContext
import com.emarsys.util.JsonUtil
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

interface ConfigInstance : ConfigInternalApi, Activatable

class Config<Logging : ConfigInstance, Gatherer : ConfigInstance, Internal : ConfigInstance>(
    loggingApi: Logging,
    gathererApi: Gatherer,
    internalApi: Internal,
    sdkContext: SdkContextApi,
    private val deviceInfoCollector: DeviceInfoCollectorApi
) : GenericApi<Logging, Gatherer, Internal>(
    loggingApi,
    gathererApi,
    internalApi,
    sdkContext
), ConfigApi {
    override val contactFieldId: Int?
        get() = sdkContext.contactFieldId
    override val applicationCode: String?
        get() = sdkContext.config?.applicationCode
    override val merchantId: String?
        get() = sdkContext.config?.merchantId
    override val clientId: String
        get() = deviceInfoCollector.getClientId()
    override val languageCode: String
        get() = getDeviceInfo().language

    override val sdkVersion: String
        get() = getDeviceInfo().sdkVersion

    override suspend fun changeApplicationCode(applicationCode: String): Result<Unit> =
        runCatching {
            withContext(sdkContext.sdkDispatcher) {
                withLogContext(buildJsonObject {
                    put(
                        "applicationCode",
                        JsonPrimitive(applicationCode)
                    )
                }) {
                    activeInstance<ConfigInternalApi>().changeApplicationCode(applicationCode)
                }
            }
        }

    override suspend fun changeMerchantId(merchantId: String): Result<Unit> = runCatching {
        withContext(sdkContext.sdkDispatcher) {
            withLogContext(buildJsonObject {
                put("merchantId", JsonPrimitive(merchantId))
            }) {
                activeInstance<ConfigInternalApi>().changeMerchantId(merchantId)
            }
        }
    }

    override suspend fun getPushSettings(): PushSettings {
        return deviceInfoCollector.getPushSettings()
    }

    private fun getDeviceInfo(): DeviceInfo {
        return JsonUtil.json.decodeFromString<DeviceInfo>(deviceInfoCollector.collect())
    }
}