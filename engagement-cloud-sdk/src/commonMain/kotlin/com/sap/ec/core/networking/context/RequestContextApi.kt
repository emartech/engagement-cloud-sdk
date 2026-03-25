package com.sap.ec.core.networking.context

internal interface RequestContextApi {

    var contactToken: String?

    var refreshToken: String?

    var clientId: String?

    var clientState: String?

    var deviceEventState: String?

    var isContactLinked: Boolean?

    fun clearTokens()

}