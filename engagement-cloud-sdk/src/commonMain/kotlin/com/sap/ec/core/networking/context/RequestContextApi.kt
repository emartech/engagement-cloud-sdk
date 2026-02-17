package com.sap.ec.core.networking.context

interface RequestContextApi {

    var contactToken: String?

    var refreshToken: String?

    var clientId: String?

    var clientState: String?

    var deviceEventState: String?

    fun clearTokens()

}