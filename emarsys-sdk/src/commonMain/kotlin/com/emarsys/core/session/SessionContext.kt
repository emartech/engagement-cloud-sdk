package com.emarsys.core.session

data class SessionContext(
    var contactToken: String? = null,
    var refreshToken: String? = null,
    var contactFieldValue: String? = null,
    var openIdToken: String? = null,
    var clientId: String? = null,
    var clientState: String? = null,
    var deviceEventState: String? = null,
    var sessionId: SessionId? = null
) {

    fun hasContactIdentification() =
        contactToken != null || contactFieldValue != null || openIdToken != null
}