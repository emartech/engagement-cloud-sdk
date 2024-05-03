package com.emarsys.mobileengage.push

import com.emarsys.core.mapper.Mapper
import kotlinx.serialization.json.JsonObject

class PushMessageMapper: Mapper<JsonObject, PushMessage> {
    override fun map(from: JsonObject): PushMessage {
        TODO("Not yet implemented")
    }

}