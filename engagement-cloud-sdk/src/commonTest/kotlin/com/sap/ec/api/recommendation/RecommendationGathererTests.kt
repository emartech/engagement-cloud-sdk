package com.sap.ec.api.recommendation

import com.sap.ec.core.exceptions.SdkException.RecommendationApiNotReady
import com.sap.ec.core.log.Logger
import com.sap.ec.event.SdkEvent
import com.sap.ec.recommendation.models.requestRecommendation.RecommendationLogic
import com.sap.ec.recommendation.models.requestRecommendation.RecommendationOptions
import dev.mokkery.MockMode
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

class RecommendationGathererTests {
    private lateinit var recommendationGatherer: RecommendationGatherer
    private lateinit var mockSdkLogger: Logger

    @BeforeTest
    fun setup() {
        mockSdkLogger = mock(MockMode.autofill)
        recommendationGatherer =
            RecommendationGatherer(
                mockSdkLogger
            )
    }

    @Test
    fun testGathering_shouldThrow() = runTest {
        val event = SdkEvent.Internal.Sdk.RequestRecommendation(
            options = RecommendationOptions(
                RecommendationLogic.HOME
            )
        )
        assertFailsWith<RecommendationApiNotReady> {
            recommendationGatherer.requestRecommendation(event)
        }
    }
}