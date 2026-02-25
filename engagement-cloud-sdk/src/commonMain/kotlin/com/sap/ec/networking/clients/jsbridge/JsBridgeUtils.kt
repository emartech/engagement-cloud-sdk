package com.sap.ec.networking.clients.jsbridge

import io.ktor.http.Headers

internal fun parseMd5FromGoogHash(headers: Headers): String? {
    val googHashValues = headers.getAll("x-goog-hash") ?: return null
    return googHashValues
        .flatMap { it.split(",") }
        .map { it.trim() }
        .firstOrNull { it.startsWith("md5=") }
        ?.removePrefix("md5=")
}
