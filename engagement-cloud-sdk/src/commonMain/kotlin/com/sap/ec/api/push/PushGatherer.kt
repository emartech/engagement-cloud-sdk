package com.sap.ec.api.push

import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.storage.StringStorageApi

internal open class PushGatherer(
    private val context: PushContextApi,
    private val storage: StringStorageApi,
    private val sdkContext: SdkContextApi,
) : PushInstance {
    override suspend fun registerPushToken(pushToken: String) {
        context.calls.add(PushCall.RegisterPushToken(pushToken))
    }

    override suspend fun clearPushToken() {
        context.calls.add(PushCall.ClearPushToken(sdkContext.config?.applicationCode))
    }

    override suspend fun getPushToken(): String? = storage.get(PushConstants.PUSH_TOKEN_STORAGE_KEY)

    override suspend fun activate() {
    }
}