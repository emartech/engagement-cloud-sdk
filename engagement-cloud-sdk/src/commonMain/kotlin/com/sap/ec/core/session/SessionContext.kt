package com.sap.ec.core.session

internal data class SessionContext(
    var sessionId: SessionId? = null,
    var sessionStart: Long? = null
)