package com.emarsys.core.networking.model

import com.emarsys.util.JsonUtil
import io.ktor.http.*

data class Response(
    val originalRequest: UrlRequest,
    val status: HttpStatusCode,
    val headers: Headers,
    val bodyAsText: String
)

inline fun <reified T> Response.body(): T {
    return JsonUtil.json.decodeFromString<T>(this.bodyAsText)
}