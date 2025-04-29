package com.emarsys.api.config

import Activatable
import com.emarsys.api.generic.GenericApi
import com.emarsys.context.SdkContextApi
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.device.DeviceInfoCollectorApi
import com.emarsys.core.device.NotificationSettings
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
    override suspend fun getContactFieldId(): Int? = sdkContext.contactFieldId

    override suspend fun getApplicationCode(): String? = sdkContext.config?.applicationCode

    override suspend fun getMerchantId(): String? = sdkContext.config?.merchantId

    override suspend fun getClientId(): String = deviceInfoCollector.getClientId()

    override suspend fun getLanguageCode(): String = getDeviceInfo().language

    override suspend fun getSdkVersion(): String = getDeviceInfo().sdkVersion

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

    override suspend fun setLanguage(language: String): Result<Unit> = runCatching {
        withContext(sdkContext.sdkDispatcher) {
            withLogContext(buildJsonObject {
                put("language", JsonPrimitive(language))
            }) {
                activeInstance<ConfigInternalApi>().setLanguage(language)
            }
        }
    }

    override suspend fun resetLanguage(): Result<Unit> = runCatching {
        withContext(sdkContext.sdkDispatcher) {
            withLogContext(buildJsonObject {}) {
                activeInstance<ConfigInternalApi>().resetLanguage()
            }
        }
    }

    override suspend fun getNotificationSettings(): NotificationSettings {
        return deviceInfoCollector.getNotificationSettings()
    }

    private suspend fun getDeviceInfo(): DeviceInfo {
        return JsonUtil.json.decodeFromString<DeviceInfo>(deviceInfoCollector.collect())
    }
}