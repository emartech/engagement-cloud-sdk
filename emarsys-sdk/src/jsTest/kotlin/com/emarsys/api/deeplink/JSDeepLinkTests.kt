package com.emarsys.api.deeplink

import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.assertions.throwables.shouldThrow
import io.ktor.http.Url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.await
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class JSDeepLinkTests {
    private companion object {
        val testUrl = Url("https://sap.com")
    }

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun trackDeepLink_shouldCall_trackDeepLink_onDeepLinkApi() = runTest {
        val mockDeepLinkApi: DeepLinkApi = mock()
        everySuspend { mockDeepLinkApi.trackDeepLink(testUrl) } returns Result.success(Unit)
        val jsDeepLink = JSDeepLink(mockDeepLinkApi, TestScope())

        jsDeepLink.trackDeepLink(testUrl).await()

        verifySuspend { mockDeepLinkApi.trackDeepLink(testUrl) }
    }

    @Test
    fun trackDeepLink_shouldThrowException_ifTrackingFails() = runTest {
        val mockDeepLinkApi: DeepLinkApi = mock()
        everySuspend { mockDeepLinkApi.trackDeepLink(testUrl) } returns Result.failure(Exception())
        val jsDeepLink = JSDeepLink(mockDeepLinkApi, TestScope())

        shouldThrow<Exception> { jsDeepLink.trackDeepLink(testUrl).await() }
    }
}