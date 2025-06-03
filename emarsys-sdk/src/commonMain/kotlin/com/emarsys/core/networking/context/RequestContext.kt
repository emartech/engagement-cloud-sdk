package com.emarsys.core.networking.context

import kotlinx.serialization.json.JsonObject

data class RequestContext(
    var contactToken: String? = null,
    var refreshToken: String? = null,
    var clientId: String? = null,
    var clientState: String? = null,
    var deviceEventState: JsonObject? = null
): RequestContextApi {

    override fun clearTokens() {
        contactToken = null
        refreshToken = null
    }
}