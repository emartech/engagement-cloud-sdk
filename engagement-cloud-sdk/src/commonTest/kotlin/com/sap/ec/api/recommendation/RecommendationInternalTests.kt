package com.sap.ec.api.recommendation

import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.channel.SdkEventWaiterApi
import com.sap.ec.core.providers.InstantProvider
import com.sap.ec.core.providers.UuidProviderApi
import com.sap.ec.event.SdkEvent
import com.sap.ec.recommendation.models.RecommendationResult
import com.sap.ec.recommendation.models.product.Product
import com.sap.ec.recommendation.models.requestRecommendation.RecommendationLogic
import com.sap.ec.recommendation.models.requestRecommendation.RecommendationOptions
import com.sap.ec.recommendation.models.tracking.ItemViewEvent
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture.Companion.slot
import dev.mokkery.matcher.capture.SlotCapture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.getIfPresent
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Instant


class RecommendationInternalTests {
    private companion object {
        const val UUID = "testId"
        const val TEST_ITEM_ID = "testItemId"
        val testTimestamp = Instant.DISTANT_FUTURE
        val recommendationOptions = RecommendationOptions(RecommendationLogic.Home)
        val recommendationTrackEvent = ItemViewEvent(TEST_ITEM_ID)
    }

    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi
    private lateinit var recommendationInternal: RecommendationInstance
    private lateinit var mockTimestampProvider: InstantProvider
    private lateinit var mockUUIDProvider: UuidProviderApi
    private lateinit var mockWaiter: SdkEventWaiterApi
    private lateinit var eventSlot: SlotCapture<SdkEvent>

    @BeforeTest
    fun setup() {
        eventSlot = slot()
        mockSdkEventDistributor = mock(MockMode.autofill)
        mockUUIDProvider = mock(MockMode.autofill)
        every { mockUUIDProvider.provide() } returns UUID
        mockTimestampProvider = mock(MockMode.autofill)
        every { mockTimestampProvider.provide() } returns testTimestamp
        mockWaiter = mock(MockMode.autofill)
        recommendationInternal =
            RecommendationInternal(
                mockSdkEventDistributor,
                mockUUIDProvider,
                mockTimestampProvider,
                sdkLogger = mock(MockMode.autofill)
            )
    }

    @Test
    fun testRequestRecommendation_shouldReturnMappedRecommendationResults_onSuccess() = runTest {
        val expectedRecommendationResult: Map<String, RecommendationResult> = mapOf(
            "feature_1" to RecommendationResult(
                "feature_1",
                topicLabel = "topicLabel",
                hasMore = false,
                listOf("testMerchant"),
                listOf(
                    Product(
                        productId = "p1",
                        title = "Product 1",
                        linkUrl = "https://example.com/p1"
                    )
                )
            )

        )
        val successResponse = SdkEvent.Internal.Sdk.Answer.Response(
            originId = "any",
            result = Result.success(expectedRecommendationResult)
        )
        everySuspend { mockWaiter.await<Map<String, RecommendationResult>>() } returns successResponse
        everySuspend { mockSdkEventDistributor.registerEvent(capture(eventSlot)) } returns mockWaiter

        val result = recommendationInternal.requestRecommendation(
            recommendationOptions,
            recommendationTrackEvent
        )

        result shouldBe expectedRecommendationResult

        with(eventSlot.getIfPresent() as SdkEvent.Internal.Sdk.RequestRecommendation) {
            contextEvent shouldBe SdkEvent.External.RecommendationTrackEvent.ItemView(
                TEST_ITEM_ID,
                UUID,
                testTimestamp,
                0
            )
        }
    }

    @Test
    fun testRequestRecommendation_shouldOmitContextEvent_ifItWasNull() = runTest {
        val recommendationResult: Map<String, RecommendationResult> = mapOf(
            "feature_1" to RecommendationResult(
                "feature_1",
                topicLabel = "topicLabel",
                hasMore = false,
                listOf("testMerchant"),
                listOf(
                    Product(
                        productId = "p1",
                        title = "Product 1",
                        linkUrl = "https://example.com/p1"
                    )
                )
            )

        )
        val successResponse = SdkEvent.Internal.Sdk.Answer.Response(
            originId = "any",
            result = Result.success(recommendationResult)
        )
        everySuspend { mockWaiter.await<Map<String, RecommendationResult>>() } returns successResponse
        everySuspend { mockSdkEventDistributor.registerEvent(capture(eventSlot)) } returns mockWaiter

        recommendationInternal.requestRecommendation(recommendationOptions, null)

        with(eventSlot.getIfPresent() as SdkEvent.Internal.Sdk.RequestRecommendation) {
            contextEvent shouldBe null
        }
    }

    @Test
    fun testRequestRecommendation_shouldReturnEmptyMap_onFailure() = runTest {
        val failureResponse =
            SdkEvent.Internal.Sdk.Answer.Response<Map<String, RecommendationResult>>(
                originId = "any",
                result = Result.failure(Exception("test error"))
            )
        everySuspend { mockWaiter.await<Map<String, RecommendationResult>>() } returns failureResponse
        everySuspend { mockSdkEventDistributor.registerEvent(any()) } returns mockWaiter

        val result = recommendationInternal.requestRecommendation(
            recommendationOptions,
            recommendationTrackEvent
        )

        result shouldBe mapOf()
    }
}