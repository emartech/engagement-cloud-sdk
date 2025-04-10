package com.emarsys.mobileengage.push

import com.emarsys.core.factory.SuspendFactory
import com.emarsys.mobileengage.push.mapper.AndroidPushV2Mapper
import com.emarsys.mobileengage.push.mapper.SilentAndroidPushV2Mapper
import com.emarsys.mobileengage.push.model.AndroidPush
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

internal class AndroidPushMessageFactory(
    private val androidPushV2Mapper: AndroidPushV2Mapper,
    private val silentAndroidPushV2Mapper: SilentAndroidPushV2Mapper
): SuspendFactory<JsonObject, AndroidPush?> {

    private companion object {
        private const val VERSION_KEY = "ems.version"
    }

    override suspend fun create(value: JsonObject): AndroidPush? {
        // TODO: check for exact version instead of just presence
        return if (value[VERSION_KEY]?.jsonPrimitive?.contentOrNull != null) {
            if (value["notification.silent"]?.jsonPrimitive?.contentOrNull == "true") {
                silentAndroidPushV2Mapper.map(value)
            } else {
                androidPushV2Mapper.map(value)
            }
        } else {
            null
        }
    }
}