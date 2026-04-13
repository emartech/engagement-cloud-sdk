package com.sap.ec.core.networking.context

import com.sap.ec.InternalSdkApi

@InternalSdkApi
interface RequestContextApi {

    var contactToken: String?

    var refreshToken: String?

    var clientId: String?

    var clientState: String?

    var deviceEventState: String?

    var isContactLinked: Boolean?

    fun clearTokens()

}