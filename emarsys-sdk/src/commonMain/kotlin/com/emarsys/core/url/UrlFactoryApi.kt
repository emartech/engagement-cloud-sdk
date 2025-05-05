package com.emarsys.core.url

import io.ktor.http.Url

internal interface UrlFactoryApi {
    fun create(urlType: EmarsysUrlType, applicationCode: String? = null): Url

}
