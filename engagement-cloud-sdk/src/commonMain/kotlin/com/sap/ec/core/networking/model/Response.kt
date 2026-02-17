package com.sap.ec.core.networking.model

import com.sap.ec.util.JsonUtil
import io.ktor.http.Headers
import io.ktor.http.HttpStatusCode

data class Response(
    val originalRequest: UrlRequest,
    val status: HttpStatusCode,
    val headers: Headers,
    val bodyAsText: String
)

inline fun <reified T> Response.body(): T {
    return JsonUtil.json.decodeFromString<T>(this.bodyAsText)
}