package com.sap.ec.core.session

data class SessionContext(
    var sessionId: SessionId? = null,
    var sessionStart: Long? = null
)