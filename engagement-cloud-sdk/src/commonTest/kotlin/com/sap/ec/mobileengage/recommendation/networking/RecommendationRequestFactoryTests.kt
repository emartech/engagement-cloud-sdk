package com.sap.ec.mobileengage.recommendation.networking

import com.sap.ec.core.url.ECUrlType
import com.sap.ec.core.url.UrlFactoryApi
import com.sap.ec.event.SdkEvent
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.ktor.http.HttpMethod
import io.ktor.http.Url
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class RecommendationRequestFactoryTests {

    private lateinit var mockUrlFactory: UrlFactoryApi

    private companion object {
        const val RECOMMENDATION_BASE_URL = "https://recommender.scarabresearch.com/merchants/ABCDE-12345"
    }

    @BeforeTest
    fun init(){
        mockUrlFactory = mock(MockMode.autofill)

    }

    @Test
    fun test_create_shouldReturn_UrlStartingWithRecommendationBaseUrl() = runTest{
        val itemViewEvent = SdkEvent.External.WebExtendEvent.ItemView("testItem")
        everySuspend { mockUrlFactory.create(ECUrlType.Recommendation) } returns Url(RECOMMENDATION_BASE_URL)

        val result = RecommendationRequestFactory(mockUrlFactory).create(itemViewEvent)

        result.url.toString() shouldStartWith RECOMMENDATION_BASE_URL
        result.method shouldBe HttpMethod.Get
        verifySuspend { mockUrlFactory.create(ECUrlType.Recommendation) }
    }

    @Test
    fun test_create_shouldReturn_itemViewUrlPath_when_itemViewEventPassedAsArgument() = runTest {
        val itemViewEvent = SdkEvent.External.WebExtendEvent.ItemView("testItem")
        everySuspend { mockUrlFactory.create(ECUrlType.Recommendation) } returns Url(RECOMMENDATION_BASE_URL)
        val expectedUrl = "$RECOMMENDATION_BASE_URL?cp=1&v=i%3AtestItem"

        val result = RecommendationRequestFactory(mockUrlFactory).create(itemViewEvent)

        result.url.toString() shouldBe expectedUrl
        result.method shouldBe HttpMethod.Get
        verifySuspend { mockUrlFactory.create(ECUrlType.Recommendation) }
    }
}