package com.sap.ec.mobileengage.push

import com.sap.ec.core.factory.SuspendFactory
import com.sap.ec.mobileengage.push.mapper.AndroidPushV2Mapper
import com.sap.ec.mobileengage.push.mapper.HuaweiPushV2Mapper
import com.sap.ec.mobileengage.push.mapper.SilentAndroidPushV2Mapper
import com.sap.ec.mobileengage.push.mapper.SilentHuaweiPushV2Mapper
import com.sap.ec.mobileengage.push.model.AndroidPush
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal class AndroidPushMessageFactory(
    private val androidPushV2Mapper: AndroidPushV2Mapper,
    private val silentAndroidPushV2Mapper: SilentAndroidPushV2Mapper,
    private val huaweiPushV2Mapper: HuaweiPushV2Mapper,
    private val silentHuaweiPushV2Mapper: SilentHuaweiPushV2Mapper
) : SuspendFactory<JsonObject, AndroidPush?> {

    private companion object {
        private const val EMS_KEY = "ems"
        private const val VERSION_KEY = "version"
        private const val EMS_VERSION_KEY = "ems.version"
        private const val NOTIFICATION_SILENT_KEY = "notification.silent"
        private const val FCM_VERSION = "FCM_V2"
        private const val HUAWEI_VERSION = "HUAWEI_V2"
        private const val NOTIFICATION_KEY = "notification"
        private const val SILENT_KEY = "silent"
    }

    override suspend fun create(value: JsonObject): AndroidPush? {
        return if (value[EMS_VERSION_KEY]?.jsonPrimitive?.contentOrNull == FCM_VERSION) {
            if (value[NOTIFICATION_SILENT_KEY]?.jsonPrimitive?.contentOrNull == "true") {
                silentAndroidPushV2Mapper.map(value)
            } else {
                androidPushV2Mapper.map(value)
            }
        } else if (value[EMS_KEY]?.jsonObject[VERSION_KEY]?.jsonPrimitive?.contentOrNull == HUAWEI_VERSION) {
            if (value[NOTIFICATION_KEY]?.jsonObject[SILENT_KEY]?.jsonPrimitive?.booleanOrNull == true) {
                silentHuaweiPushV2Mapper.map(value)
            } else {
                huaweiPushV2Mapper.map(value)
            }
        } else null
    }
}