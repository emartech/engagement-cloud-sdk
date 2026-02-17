package com.sap.ec.api.deeplink

import io.ktor.http.Url

interface DeepLinkApi {

    fun track(url: Url): Result<Boolean>

}