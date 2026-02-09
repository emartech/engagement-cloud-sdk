package com.emarsys.context

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class DefaultUrlTests {

    private val defaultUrls = DefaultUrls(
        "clientServiceBaseUrl - origin",
        "eventServiceBaseUrl - origin",
        "deepLinkBaseUrl - origin",
        "remoteConfigBaseUrl - origin",
        "loggingUrl - origin",
        "embeddedMessagingBaseUrl - origin",
        "ecJsBridgeUrl - origin"
    )

    @Test
    fun testCopyWith() = runTest {
        val expected = DefaultUrls(
            "clientServiceBaseUrl - new",
            "eventServiceBaseUrl - origin",
            "deepLinkBaseUrl - origin",
            "remoteConfigBaseUrl - origin",
            "loggingUrl - new",
            "embeddedMessagingBaseUrl - origin",
            "ecJsBridgeUrl - origin"
        )
        val result = defaultUrls.copyWith(
            clientServiceBaseUrl = "clientServiceBaseUrl - new",
            loggingUrl = "loggingUrl - new"
        )

        result shouldBe expected
    }

}