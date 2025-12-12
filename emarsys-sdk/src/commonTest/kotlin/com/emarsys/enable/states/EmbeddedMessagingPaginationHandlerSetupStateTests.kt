package com.emarsys.enable.states

import com.emarsys.mobileengage.embeddedmessaging.pagination.EmbeddedMessagingPaginationHandlerApi
import dev.mokkery.MockMode
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EmbeddedMessagingPaginationHandlerSetupStateTests {
    private lateinit var mockEmbeddedMessagingPaginationHandler: EmbeddedMessagingPaginationHandlerApi
    private lateinit var embeddedMessagingPaginationHandlerSetupState: EmbeddedMessagingPaginationHandlerSetupState


    @BeforeTest
    fun setup() = runTest {
        mockEmbeddedMessagingPaginationHandler = mock(MockMode.autoUnit)
        embeddedMessagingPaginationHandlerSetupState =
            EmbeddedMessagingPaginationHandlerSetupState(
                mockEmbeddedMessagingPaginationHandler,
                sdkLogger = mock(MockMode.autoUnit)
            )
    }

    @Test
    fun testActive_should_registerEmbeddedMessagingPaginationHandler() = runTest {
        val result = embeddedMessagingPaginationHandlerSetupState.active()

        verifySuspend {
            mockEmbeddedMessagingPaginationHandler.register()
        }

        result shouldBe Result.success(Unit)
    }
}