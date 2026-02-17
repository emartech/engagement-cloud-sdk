package com.sap.ec.mobileengage.push.mapper

import com.sap.ec.core.log.Logger
import com.sap.ec.core.mapper.Mapper
import com.sap.ec.core.providers.UuidProviderApi
import com.sap.ec.mobileengage.push.model.AndroidPushMessage
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

internal class SilentHuaweiPushV2Mapper(
    private val uuidProvider: UuidProviderApi,
    private val logger: Logger,
    private val json: Json
) : Mapper<JsonObject, AndroidPushMessage> {
    override suspend fun map(from: JsonObject): AndroidPushMessage? {
        TODO("Not yet implemented")
    }

}