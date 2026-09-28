package com.sap.ec.event

import dev.mokkery.MockMode
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class EventPreProcessorTests {

    private lateinit var recommendationEventPreProcessor: EventPreProcessorApi<SdkEvent>
    private lateinit var customEventPreProcessor: EventPreProcessorApi<SdkEvent>

    @Test
    fun eventPreProcessor_shouldCallProcess_onItsAllPreProcessors() = runTest {
        recommendationEventPreProcessor = mock(MockMode.autofill)
        customEventPreProcessor = mock(MockMode.autofill)
        val processors = listOf(
            recommendationEventPreProcessor,
            customEventPreProcessor
        )
        val event = SdkEvent.External.RecommendationTrackEvent.Cart(listOf())

        EventPreProcessor(processors).process(event)

        verifySuspend {
            recommendationEventPreProcessor.process(event)
            customEventPreProcessor.process(event)
        }
    }
}