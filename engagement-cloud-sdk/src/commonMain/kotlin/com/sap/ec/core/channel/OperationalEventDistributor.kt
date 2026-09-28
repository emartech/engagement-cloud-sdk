package com.sap.ec.core.channel

import com.sap.ec.core.url.ECUrlType
import com.sap.ec.core.url.UrlFactoryApi
import com.sap.ec.event.SdkEvent.Internal.OperationalEvent
import com.sap.ec.event.SdkEvent.Internal.Sdk.ApplyGlobalRemoteConfig
import com.sap.ec.event.SdkEvent.Internal.Sdk.ClearPushToken
import com.sap.ec.event.SdkEvent.Internal.Sdk.UnlinkContact

internal class OperationalEventDistributor(
    private val sdkEventDistributor: SdkEventDistributorApi,
    private val urlFactory: UrlFactoryApi
) : OperationalEventDistributorApi {

    override suspend fun registerEvent(sdkEvent: OperationalEvent): SdkEventWaiterApi {
        val event = when (sdkEvent) {
            is ClearPushToken -> sdkEvent.applicationCode?.let { ECUrlType.ClearPushToken(it) }
            is UnlinkContact -> sdkEvent.applicationCode?.let { ECUrlType.UnlinkContact(it) }
            is ApplyGlobalRemoteConfig -> ECUrlType.GlobalRemoteConfig
        }?.let {
            sdkEvent.withUrl(urlFactory.create(it))
        } ?: sdkEvent

        return sdkEventDistributor.registerEvent(event)
    }
}