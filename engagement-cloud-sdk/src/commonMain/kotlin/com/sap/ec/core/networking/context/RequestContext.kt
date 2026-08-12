package com.sap.ec.core.networking.context

import com.sap.ec.InternalSdkApi
import com.sap.ec.core.storage.StorageConstants.CLIENT_STATE_STORAGE_KEY
import com.sap.ec.core.storage.StorageConstants.CONTACT_TOKEN_STORAGE_KEY
import com.sap.ec.core.storage.StorageConstants.DEVICE_EVENT_STATE_STORAGE_KEY
import com.sap.ec.core.storage.StorageConstants.IS_CONTACT_LINKED_STORAGE_KEY
import com.sap.ec.core.storage.StorageConstants.REFRESH_TOKEN_STORAGE_KEY
import com.sap.ec.core.storage.Store
import kotlinx.serialization.builtins.serializer

@InternalSdkApi
class RequestContext() : RequestContextApi {

    override var contactToken: String? by Store(
        key = CONTACT_TOKEN_STORAGE_KEY,
        serializer = String.serializer()
    )

    override var refreshToken: String? by Store(
        key = REFRESH_TOKEN_STORAGE_KEY,
        serializer = String.serializer()
    )

    override var clientId: String? = null

    override var clientState: String? by Store(
        key = CLIENT_STATE_STORAGE_KEY,
        serializer = String.serializer()
    )

    override var deviceEventState: String? by Store(
        key = DEVICE_EVENT_STATE_STORAGE_KEY,
        serializer = String.serializer()
    )

    override var isContactLinked: Boolean? by Store(
        key = IS_CONTACT_LINKED_STORAGE_KEY,
        serializer = Boolean.serializer()
    )

    override fun clearTokens() {
        contactToken = null
        refreshToken = null
    }
}