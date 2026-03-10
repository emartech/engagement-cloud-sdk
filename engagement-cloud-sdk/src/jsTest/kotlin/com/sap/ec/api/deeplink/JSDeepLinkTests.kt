package com.sap.ec.api.deeplink

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import io.kotest.matchers.shouldBe
import io.ktor.http.Url
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class JSDeepLinkTests {
    private companion object {
        const val testUrl = "https://sap.com"
    }

    @Test
    fun testTrack_shouldCall_track_onDeepLinkApi() {
        val mockDeepLinkApi: DeepLinkApi = mock()
        every { mockDeepLinkApi.track(Url(testUrl)) } returns Result.success(true)
        val jsDeepLink = JSDeepLink(mockDeepLinkApi)

        val result = jsDeepLink.track(testUrl)

        verify { mockDeepLinkApi.track(Url(testUrl)) }
        result shouldBe true
    }

    @Test
    fun testTrack_shouldReturnFalse_ifTrackingFails() {
        val mockDeepLinkApi: DeepLinkApi = mock()
        every { mockDeepLinkApi.track(Url(testUrl)) } returns Result.failure(Exception())
        val jsDeepLink = JSDeepLink(mockDeepLinkApi)

        val result = jsDeepLink.track(testUrl)

        result shouldBe false
    }

    @Test
    fun testTrack_shouldPassConvertedUrlToDeepLinkApi() {
        val mockDeepLinkApi: DeepLinkApi = mock()
        val urlString = "https://example.com?ems_dl=abc123"
        every { mockDeepLinkApi.track(Url(urlString)) } returns Result.success(true)
        val jsDeepLink = JSDeepLink(mockDeepLinkApi)

        val result = jsDeepLink.track(urlString)

        verify { mockDeepLinkApi.track(Url(urlString)) }
        result shouldBe true
    }

    @Test
    fun testTrack_shouldReturnFalse_ifUrlIsMalformed() {
        val mockDeepLinkApi: DeepLinkApi = mock()
        every { mockDeepLinkApi.track(any()) } returns Result.success(false)
        val jsDeepLink = JSDeepLink(mockDeepLinkApi)

        val result = jsDeepLink.track("not a valid url")

        result shouldBe false
    }
}