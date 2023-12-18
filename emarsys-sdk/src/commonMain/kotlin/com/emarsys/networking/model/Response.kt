package com.emarsys.networking.model

import io.ktor.http.*
import kotlinx.serialization.json.Json

data class Response(
    val originalRequest: UrlRequest,
    val status: HttpStatusCode,
    val headers: Headers,
    val bodyAsText: String
)

inline fun <reified T> Response.body(): T {
    return Json.decodeFromString<T>(this.bodyAsText)
}

fun Response.isSuccess(): Boolean {
    return status.isSuccess()
}