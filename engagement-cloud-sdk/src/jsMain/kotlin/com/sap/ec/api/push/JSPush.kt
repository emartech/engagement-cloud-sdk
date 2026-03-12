package com.sap.ec.api.push

import com.sap.ec.mobileengage.push.JsPushWrapperApi

internal class JSPush(
    private val jsPushWrapperApi: JsPushWrapperApi
) : JSPushApi {

    override suspend fun subscribe() {
        jsPushWrapperApi.subscribe().getOrThrow()
    }

    override suspend fun unsubscribe() {
        jsPushWrapperApi.unsubscribe().getOrThrow()
    }

    override suspend fun isSubscribed(): Boolean {
        return jsPushWrapperApi.isSubscribed()
    }

    override suspend fun getPermissionState(): String {
        return jsPushWrapperApi.getPermissionState()
    }
}
