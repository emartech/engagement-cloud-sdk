package com.sap.ec.api.recommendation

import com.sap.ec.core.exceptions.SdkException
import com.sap.ec.recommendation.models.RecommendationResult
import com.sap.ec.recommendation.models.requestRecommendation.RecommendationLogic
import com.sap.ec.recommendation.models.requestRecommendation.RecommendationOptions
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class IosRecommendationTests {
    private companion object {
        val testRecommendationOptions = RecommendationOptions(RecommendationLogic.Cart)
    }

    private lateinit var iosRecommendation: IosRecommendationApi
    private lateinit var mockRecommendationApi: RecommendationApi

    @BeforeTest
    fun setup() {
        mockRecommendationApi = mock(MockMode.autofill)
        iosRecommendation = IosRecommendation(mockRecommendationApi)
    }

    @Test
    fun requestRecommendation_shouldCall_internalApi_withSuppliedParameters() = runTest {
        val recommendationResult = RecommendationResult(mapOf())
        everySuspend { mockRecommendationApi.requestRecommendation(testRecommendationOptions) } returns Result.success(recommendationResult)

        val result = iosRecommendation.requestRecommendation(testRecommendationOptions)

        result shouldBe recommendationResult
    }

    @Test
    fun requestRecommendation_shouldCall_throwException_ifInternalApiReturnsFailure() = runTest {
        val testException = SdkException.MissingRecommendationConfigurationException()
        everySuspend { mockRecommendationApi.requestRecommendation(any()) } returns Result.failure(testException)

        val error = shouldThrow<SdkException.MissingRecommendationConfigurationException> {
            iosRecommendation.requestRecommendation(testRecommendationOptions)
        }

        error.message shouldBe "Missing recommendation configuration!"
    }
}