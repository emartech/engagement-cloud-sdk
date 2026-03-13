package com.sap.ec.core.url

import io.ktor.http.Url

internal interface UrlFactoryApi {
    suspend fun create(urlType: ECUrlType): Url
}
