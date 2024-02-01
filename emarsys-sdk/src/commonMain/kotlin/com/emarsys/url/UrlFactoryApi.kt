package com.emarsys.url

import io.ktor.http.*

interface UrlFactoryApi {
    fun create(urlType: EmarsysUrlType): Url

}
