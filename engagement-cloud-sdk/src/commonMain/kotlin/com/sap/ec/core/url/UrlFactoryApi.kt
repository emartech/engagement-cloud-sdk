package com.sap.ec.core.url

import com.sap.ec.event.OnlineSdkEvent
import io.ktor.http.Url

internal interface UrlFactoryApi {
    suspend fun create(urlType: ECUrlType, sdkEvent: OnlineSdkEvent? = null): Url

}
