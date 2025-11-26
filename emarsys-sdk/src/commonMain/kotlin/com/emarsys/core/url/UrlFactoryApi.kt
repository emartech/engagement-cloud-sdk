package com.emarsys.core.url

import com.emarsys.event.OnlineSdkEvent
import io.ktor.http.Url

internal interface UrlFactoryApi {
    fun create(urlType: EmarsysUrlType, sdkEvent: OnlineSdkEvent? = null): Url

}
