package com.emarsys.core.session

class SessionContext(
    var contactToken: String? = null,
    var refreshToken: String? = null,
    var contactFieldValue: String? = null,
    var openIdToken: String? = null,
    var clientId: String? = null,
    var clientState: String? = null,
    var deviceEventState: String? = null
) {

    fun hasContactIdentification() =
        contactToken != null || contactFieldValue != null || openIdToken != null
}