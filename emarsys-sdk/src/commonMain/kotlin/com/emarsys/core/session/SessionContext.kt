package com.emarsys.core.session

import kotlinx.serialization.json.JsonObject

data class SessionContext(
    var contactToken: String? = null,
    var refreshToken: String? = null,
    var contactFieldValue: String? = null,
    var openIdToken: String? = null,
    var clientId: String? = null,
    var clientState: String? = null,
    var deviceEventState: JsonObject? = null,
    var sessionId: SessionId? = null,
    var sessionStart: Long? = null
): SessionContextApi {

    fun hasContactIdentification() =
        contactToken != null || contactFieldValue != null || openIdToken != null

    override fun clearSessionTokens() {
        contactToken = null
        refreshToken = null
    }
}