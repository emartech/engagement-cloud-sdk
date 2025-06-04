package com.emarsys.core.networking.context

import kotlinx.serialization.json.JsonObject

interface RequestContextApi {

    var contactToken: String?

    var refreshToken: String?

    var clientId: String?

    var clientState: String?

    var deviceEventState: JsonObject?

    fun clearTokens()

}