package com.emarsys.core.url

import io.ktor.http.Url

interface UrlFactoryApi {
    fun create(urlType: EmarsysUrlType, applicationCode: String? = null): Url

}
