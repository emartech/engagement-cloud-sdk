package com.emarsys.api.embeddedmessaging

import com.emarsys.api.SdkState
import com.emarsys.context.SdkContextApi
import com.emarsys.networking.clients.embedded.messaging.model.MessageCategory
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.verify
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
class EmbeddedMessagingTests {

    private lateinit var mockInternalInstance: EmbeddedMessagingInstance
    private lateinit var mockLoggingInstance: EmbeddedMessagingInstance
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var embeddedMessaging: EmbeddedMessagingApi

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        mockLoggingInstance = mock(MockMode.autofill)
        every { mockLoggingInstance.categories } returns emptyList()
        mockInternalInstance = mock(MockMode.autofill)
        mockSdkContext = mock()
        every { mockSdkContext.sdkDispatcher } returns StandardTestDispatcher()

        embeddedMessaging = EmbeddedMessaging(
            mockLoggingInstance,
            mockLoggingInstance,
            mockInternalInstance,
            mockSdkContext
        )
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun categories_shouldGetCategories_fromActiveInstance_initializedSDK() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Initialized)
        embeddedMessaging.registerOnContext()

        embeddedMessaging.categories shouldBe emptyList()

        verify { mockLoggingInstance.categories }
    }

    @Test
    fun categories_shouldGetCategories_fromActiveInstance_activeSDK() = runTest {
        val testCategories = listOf(MessageCategory(1, "1"))
        every { mockInternalInstance.categories } returns testCategories

        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Active)

        embeddedMessaging.registerOnContext()

        embeddedMessaging.categories shouldBe testCategories

        verify { mockInternalInstance.categories }
    }
}