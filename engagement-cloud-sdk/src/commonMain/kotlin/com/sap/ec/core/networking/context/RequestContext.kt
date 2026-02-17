package com.sap.ec.core.networking.context

import com.sap.ec.core.storage.Store
import kotlinx.serialization.builtins.serializer

internal class RequestContext() : RequestContextApi {

    override var contactToken: String? by Store(
        key = "contactToken",
        serializer = String.serializer()
    )

    override var refreshToken: String? by Store(
        key = "refreshToken",
        serializer = String.serializer()
    )

    override var clientId: String? = null

    override var clientState: String? by Store(
        key = "clientState",
        serializer = String.serializer()
    )

    override var deviceEventState: String? by Store(
        key = "deviceEventState",
        serializer = String.serializer()
    )

    override fun clearTokens() {
        contactToken = null
        refreshToken = null
    }
}