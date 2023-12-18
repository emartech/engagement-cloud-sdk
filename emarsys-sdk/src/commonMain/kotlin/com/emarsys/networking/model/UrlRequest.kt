package com.emarsys.networking.model

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*

data class UrlRequest(
    val urlString: Url,
    val method: HttpMethod,
    val bodyString: String? = null,
    val headers: Map<String, List<String>>? = null
)