package com.emarsys.api.config

import Activatable
import com.emarsys.api.generic.GenericApi
import com.emarsys.context.SdkContextApi
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.device.DeviceInfoCollectorApi
import com.emarsys.core.device.PushSettings
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

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
    override val hardwareId: String
        get() = deviceInfoCollector.getHardwareId()
    override val languageCode: String
        get() = getDeviceInfo().languageCode
    override val pushSettings: PushSettings
        get() = deviceInfoCollector.getPushSettings()

    override val sdkVersion: String
        get() = getDeviceInfo().sdkVersion

    override suspend fun changeApplicationCode(applicationCode: String): Result<Unit> =
        runCatching {
            withContext(sdkContext.sdkDispatcher) {
                activeInstance<ConfigInternalApi>().changeApplicationCode(applicationCode)
            }
        }

    override suspend fun changeMerchantId(merchantId: String): Result<Unit> = runCatching {
        withContext(sdkContext.sdkDispatcher) {
            activeInstance<ConfigInternalApi>().changeMerchantId(merchantId)
        }
    }

    private fun getDeviceInfo(): DeviceInfo {
        return Json.decodeFromString<DeviceInfo>(deviceInfoCollector.collect())
    }
}