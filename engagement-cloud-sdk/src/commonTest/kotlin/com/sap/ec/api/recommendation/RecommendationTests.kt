package com.sap.ec.api.recommendation

import com.sap.ec.api.SdkState
import com.sap.ec.context.SdkContextApi
import com.sap.ec.event.SdkEvent
import com.sap.ec.recommendation.RecommendationLogic
import com.sap.ec.recommendation.RecommendationOptions
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecommendationTests {
    private companion object {
        val event = SdkEvent.Internal.Sdk.RequestRecommendation(
            options = RecommendationOptions(
                RecommendationLogic.HOME
            )
        )
        val testException = Exception()
    }

    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var mockLoggingRecommendation: RecommendationInstance
    private lateinit var mockGathererRecommendation: RecommendationInstance
    private lateinit var mockRecommendationInternal: RecommendationInstance
    private lateinit var recommendation: Recommendation<RecommendationInstance, RecommendationInstance, RecommendationInstance>

    @BeforeTest
    fun setup() {
        val mainDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(mainDispatcher)

        mockLoggingRecommendation = mock(MockMode.autofill)
        mockGathererRecommendation = mock(MockMode.autofill)
        mockRecommendationInternal = mock(MockMode.autofill)
        mockSdkContext = mock(MockMode.autofill)

        every { mockSdkContext.sdkDispatcher } returns mainDispatcher

        recommendation =
            Recommendation(
                mockLoggingRecommendation,
                mockGathererRecommendation,
                mockRecommendationInternal,
                mockSdkContext
            )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testRequestRecommendation_initializedState() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Initialized)
        recommendation.registerOnContext()

        recommendation.requestRecommendation(event)

        verifySuspend { mockLoggingRecommendation.requestRecommendation(event) }
    }

    @Test
    fun testRequestRecommendation_unInitializedState() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.UnInitialized)
        recommendation.registerOnContext()

        recommendation.requestRecommendation(event)

        verifySuspend { mockLoggingRecommendation.requestRecommendation(event) }
    }

    @Test
    fun testRequestRecommendation_onHoldState() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.OnHold)
        recommendation.registerOnContext()

        recommendation.requestRecommendation(event)

        verifySuspend { mockGathererRecommendation.requestRecommendation(event) }
    }

    @Test
    fun testRequestRecommendation_activeState() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Active)
        recommendation.registerOnContext()

        recommendation.requestRecommendation(event)

        verifySuspend { mockRecommendationInternal.requestRecommendation(event) }
    }

    @Test
    fun testRequestRecommendation_activeState_throws() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Active)
        everySuspend { mockRecommendationInternal.requestRecommendation(event) } throws testException
        recommendation.registerOnContext()

        val result = recommendation.requestRecommendation(event)

        result.exceptionOrNull() shouldBe testException
    }
}