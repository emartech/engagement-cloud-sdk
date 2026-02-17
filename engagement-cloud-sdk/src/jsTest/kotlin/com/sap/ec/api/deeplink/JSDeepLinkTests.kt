package com.sap.ec.api.deeplink

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.verify
import io.kotest.matchers.shouldBe
import io.ktor.http.Url
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class JSDeepLinkTests {
    private companion object {
        val testUrl = Url("https://sap.com")
    }

    @Test
    fun testTrack_shouldCall_track_onDeepLinkApi() {
        val mockDeepLinkApi: DeepLinkApi = mock()
        every { mockDeepLinkApi.track(testUrl) } returns Result.success(true)
        val jsDeepLink = JSDeepLink(mockDeepLinkApi)

        val result = jsDeepLink.track(testUrl)

        verify { mockDeepLinkApi.track(testUrl) }
        result shouldBe true
    }

    @Test
    fun testTrack_shouldReturnFalse_ifTrackingFails() {
        val mockDeepLinkApi: DeepLinkApi = mock()
        every { mockDeepLinkApi.track(testUrl) } returns Result.failure(Exception())
        val jsDeepLink = JSDeepLink(mockDeepLinkApi)

        val result = jsDeepLink.track(testUrl)

        result shouldBe false
    }
}