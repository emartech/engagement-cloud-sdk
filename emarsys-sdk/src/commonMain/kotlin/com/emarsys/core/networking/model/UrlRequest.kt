package com.emarsys.core.networking.model

import io.ktor.http.HttpMethod
import io.ktor.http.Url

data class UrlRequest(
    val url: Url,
    val method: HttpMethod,
    val bodyString: String? = null,
    val headers: Map<String, Any?>? = null,
    val shouldRetryOnFail: Boolean = true,
    val isLogRequest: Boolean = false
)