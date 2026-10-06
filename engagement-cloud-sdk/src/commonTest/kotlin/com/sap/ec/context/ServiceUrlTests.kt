package com.sap.ec.context

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class ServiceUrlTests {

    private val serviceUrls = ServiceUrls(
        "clientServiceBaseUrl - origin",
        "eventServiceBaseUrl - origin",
        "deepLinkBaseUrl - origin",
        "remoteConfigBaseUrl - origin",
        "loggingUrl - origin",
        "embeddedMessagingBaseUrl - origin",
        "ecJsBridgeUrl - origin",
        "jsBridgeSignatureUrl - origin"
    )

    @Test
    fun testCopyWith() = runTest {
        val expected = ServiceUrls(
            "clientServiceBaseUrl - new",
            "eventServiceBaseUrl - origin",
            "deepLinkBaseUrl - origin",
            "remoteConfigBaseUrl - origin",
            "loggingUrl - new",
            "embeddedMessagingBaseUrl - origin",
            "ecJsBridgeUrl - origin",
            "jsBridgeSignatureUrl - origin"
        )
        val result = serviceUrls.copyWith(
            clientServiceBaseUrl = "clientServiceBaseUrl - new",
            loggingUrl = "loggingUrl - new"
        )

        result shouldBe expected
    }

}