package com.emarsys.core.networking.model

import io.ktor.http.*

data class UrlRequest(
    val url: Url,
    val method: HttpMethod,
    val bodyString: String? = null,
    val headers: Map<String, List<String>>? = null,
    val shouldRetryOnFail: Boolean = true
)