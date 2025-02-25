package com.emarsys.api.deepLink

import com.emarsys.context.DefaultUrls
import com.emarsys.context.SdkContext
import com.emarsys.context.SdkContextApi
import com.emarsys.core.log.LogLevel
import com.emarsys.networking.clients.deepLink.DeepLinkClientApi
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.resetAnswers
import dev.mokkery.verifySuspend
import io.ktor.http.Url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@Suppress("OPT_IN_USAGE")
class DeepLinkInternalTests {

    private lateinit var mockDeepLinkClient: DeepLinkClientApi
    private lateinit var sdkContext: SdkContextApi

    private lateinit var deepLinkInternal: DeepLinkInternal

    private val mainDispatcher = StandardTestDispatcher()

    init {
        Dispatchers.setMain(mainDispatcher)
    }

    @BeforeTest
    fun setUp() {
        mockDeepLinkClient = mock()
        sdkContext = SdkContext(
            StandardTestDispatcher(),
            mainDispatcher,
            DefaultUrls("", "", "", "", "", "", ""),
            LogLevel.Error,
            mutableSetOf()
        )

        deepLinkInternal = DeepLinkInternal(sdkContext, mockDeepLinkClient)
    }

    @AfterTest
    fun tearDown() {
        resetAnswers(mockDeepLinkClient)
    }

    @Test
    fun testTrackDeepLink_should_extract_ems_dl_fromUrl_andTrackDeepLink_onDeepLinkClient() = runTest {
        val url = Url("https://example.com?ems_dl=123")

        everySuspend { mockDeepLinkClient.trackDeepLink("123") }.returns(Unit)

        deepLinkInternal.trackDeepLink(url)

        verifySuspend { mockDeepLinkClient.trackDeepLink("123") }
    }
}
