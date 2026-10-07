package com.sap.ec.api.recommendation

import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.channel.SdkEventWaiterApi
import com.sap.ec.event.SdkEvent
import com.sap.ec.recommendation.models.product.Product
import com.sap.ec.recommendation.models.requestRecommendation.RecommendationLogic
import com.sap.ec.recommendation.models.requestRecommendation.RecommendationOptions
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class RecommendationInternalTests {
    private companion object {
        val event = SdkEvent.Internal.Sdk.RequestRecommendation(
            options = RecommendationOptions(
                RecommendationLogic.Home
            )
        )
    }

    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi
    private lateinit var recommendationInternal: RecommendationInstance
    private lateinit var mockWaiter: SdkEventWaiterApi

    @BeforeTest
    fun setup() {
        mockSdkEventDistributor = mock(MockMode.autofill)
        everySuspend { mockSdkEventDistributor.registerEvent(any()) } returns mock(MockMode.autofill)
        mockWaiter = mock(MockMode.autofill)
        recommendationInternal = RecommendationInternal(mockSdkEventDistributor, sdkLogger = mock(MockMode.autofill))
    }

    @Test
    fun testRequestRecommendation_shouldReturnProducts_onSuccess() = runTest {
        val expectedProducts = listOf(
            Product(productId = "p1", title = "Product 1", linkUrl = "https://example.com/p1")
        )
        val successResponse = SdkEvent.Internal.Sdk.Answer.Response(
            originId = "any",
            result = Result.success(expectedProducts)
        )
        everySuspend { mockWaiter.await<List<Product>>() } returns successResponse
        everySuspend { mockSdkEventDistributor.registerEvent(event) } returns mockWaiter

        val result = recommendationInternal.requestRecommendation(event)

        result shouldBe expectedProducts
    }

    @Test
    fun testRequestRecommendation_shouldReturnEmptyList_onFailure() = runTest {
        val failureResponse = SdkEvent.Internal.Sdk.Answer.Response<List<Product>>(
            originId = "any",
            result = Result.failure(Exception("test error"))
        )
        everySuspend { mockWaiter.await<List<Product>>() } returns failureResponse
        everySuspend { mockSdkEventDistributor.registerEvent(event) } returns mockWaiter

        val result = recommendationInternal.requestRecommendation(event)

        result shouldBe emptyList()
    }
}