package com.emarsys.core.url

import io.ktor.http.*

interface UrlFactoryApi {
    fun create(urlType: EmarsysUrlType): Url

}
