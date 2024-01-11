package com.emarsys.session

class SessionContext(
    var contactToken: String? = null,
    var refreshToken: String? = null,
    var clientId: String? = null,
    var clientState: String? = null
)