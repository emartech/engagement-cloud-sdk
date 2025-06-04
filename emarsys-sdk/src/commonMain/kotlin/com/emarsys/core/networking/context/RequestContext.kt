package com.emarsys.core.networking.context

import com.emarsys.core.storage.Store
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonObject

internal class RequestContext(): RequestContextApi {

   override var contactToken: String? by Store(
        key = "contactToken",
        serializer = String.serializer()
    )

    override var refreshToken: String? by Store(
        key = "refreshToken",
        serializer = String.serializer()
    )

    override var clientId: String? by Store(
        key = "clientId",
        serializer = String.serializer()
    )

    override var clientState: String? by Store(
        key = "clientState",
        serializer = String.serializer()
    )

    override var deviceEventState: JsonObject? by Store(
        key = "deviceEventState",
        serializer = JsonObject.serializer()
    )

    override fun clearTokens() {
        contactToken = null
        refreshToken = null
    }
}