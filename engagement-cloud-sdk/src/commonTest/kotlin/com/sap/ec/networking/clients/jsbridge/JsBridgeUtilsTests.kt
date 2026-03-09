package com.sap.ec.networking.clients.jsbridge

import io.kotest.matchers.shouldBe
import io.ktor.http.headersOf
import kotlin.test.Test

class JsBridgeUtilsTests {

    @Test
    fun parseMd5FromGoogHash_shouldReturnMd5Header_withoutPrefix() {
        val md5Header = "importantHeader"
        val testHeaders = headersOf(
            Pair("x-goog-hash", listOf("someGoogleHeader", "otherGoogleHeader")),
            Pair("X-Goog-Hash", listOf("md5=$md5Header")),
            Pair("ignoreMe", listOf("someData"))
        )

        val result = parseMd5FromGoogHash(testHeaders)

        result shouldBe md5Header
    }

    @Test
    fun parseMd5FromGoogHash_shouldReturnNull_whenMd5HeaderIsMissing() {
        val testHeaders = headersOf(
            Pair("x-goog-hash", listOf("someGoogleHeader", "otherGoogleHeader")),
            Pair("ignoreMe", listOf("someData"))
        )

        val result = parseMd5FromGoogHash(testHeaders)

        result shouldBe null
    }
}