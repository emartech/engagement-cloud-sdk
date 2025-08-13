package com.emarsys.context

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class DefaultUrlTests {

    private val defaultUrls = DefaultUrls(
        "clientServiceBaseUrl - origin",
        "eventServiceBaseUrl - origin",
        "deepLinkBaseUrl - origin",
        "inboxBaseUrl - origin",
        "remoteConfigBaseUrl - origin",
        "loggingUrl - origin"
    )

    @Test
    fun testCopyWith() = runTest {
        val expected = DefaultUrls(
            "clientServiceBaseUrl - new",
            "eventServiceBaseUrl - origin",
            "deepLinkBaseUrl - origin",
            "inboxBaseUrl - new",
            "remoteConfigBaseUrl - origin",
            "loggingUrl - new"
        )
        val result = defaultUrls.copyWith(
            clientServiceBaseUrl = "clientServiceBaseUrl - new",
            inboxBaseUrl = "inboxBaseUrl - new",
            loggingUrl = "loggingUrl - new",
            remoteConfigBaseUrl = null
        )

        result shouldBe expected
    }

}