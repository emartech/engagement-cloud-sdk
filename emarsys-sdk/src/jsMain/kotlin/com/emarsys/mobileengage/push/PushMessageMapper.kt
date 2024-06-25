package com.emarsys.mobileengage.push

import com.emarsys.core.mapper.Mapper
import com.emarsys.mobileengage.push.model.JsPushMessage
import kotlinx.serialization.json.JsonObject

class PushMessageMapper : Mapper<JsonObject, JsPushMessage> {
    override fun map(from: JsonObject): JsPushMessage {
        TODO("Not yet implemented")
    }

}