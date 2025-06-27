package com.emarsys.mobileengage.push.mapper

import com.emarsys.core.log.Logger
import com.emarsys.core.mapper.Mapper
import com.emarsys.core.providers.UuidProviderApi
import com.emarsys.mobileengage.push.model.AndroidPushMessage
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