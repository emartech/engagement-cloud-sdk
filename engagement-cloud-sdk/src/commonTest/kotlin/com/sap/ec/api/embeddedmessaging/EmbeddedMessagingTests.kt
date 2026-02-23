package com.sap.ec.api.embeddedmessaging

import com.sap.ec.api.SdkState
import com.sap.ec.context.SdkContextApi
import com.sap.ec.networking.clients.embedded.messaging.model.MessageCategory
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
        every { mockLoggingInstance.categories } returns emptyList()

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

    @Test
    fun isUnreadFilterActive_shouldGetValue_fromActiveInstance_initializedSDK() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Initialized)
        every { mockLoggingInstance.isUnreadFilterActive } returns false

        embeddedMessaging.registerOnContext()

        embeddedMessaging.isUnreadFilterActive shouldBe false
        verify { mockLoggingInstance.isUnreadFilterActive }
    }

    @Test
    fun isUnreadFilterActive_shouldGetValue_fromActiveInstance_activeSDK() = runTest {
        every { mockInternalInstance.isUnreadFilterActive } returns true
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Active)

        embeddedMessaging.registerOnContext()

        embeddedMessaging.isUnreadFilterActive shouldBe true
        verify { mockInternalInstance.isUnreadFilterActive }
    }

    @Test
    fun activeCategoryFilters_shouldGetValue_fromActiveInstance_initializedSDK() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Initialized)
        every { mockLoggingInstance.activeCategoryFilters } returns emptySet()

        embeddedMessaging.registerOnContext()

        embeddedMessaging.activeCategoryFilters shouldBe emptyList()
        verify { mockLoggingInstance.activeCategoryFilters }
    }

    @Test
    fun activeCategoryFilters_shouldGetValue_fromActiveInstance_activeSDK() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Active)
        every { mockInternalInstance.activeCategoryFilters } returns emptySet()

        embeddedMessaging.registerOnContext()

        embeddedMessaging.activeCategoryFilters shouldBe emptyList()
        verify { mockInternalInstance.activeCategoryFilters }
    }

    @Test
    fun filterUnreadOnly_shouldCallMethod_onActiveInstance_initializedSDK() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Initialized)
        embeddedMessaging.registerOnContext()

        embeddedMessaging.filterUnreadOnly(true)

        verify { mockLoggingInstance.filterUnreadOnly(true) }
    }

    @Test
    fun filterUnreadOnly_shouldCallMethod_onActiveInstance_activeSDK() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Active)
        embeddedMessaging.registerOnContext()

        embeddedMessaging.filterUnreadOnly(true)

        verify { mockInternalInstance.filterUnreadOnly(true) }
    }

    @Test
    fun filterByCategories_shouldCallMethod_onActiveInstance_initializedSDK() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Initialized)
        embeddedMessaging.registerOnContext()

        val categories = listOf(MessageCategory(1, "Category 1"), MessageCategory(2, "Category 2"))
        embeddedMessaging.filterByCategories(categories)

        verify { mockLoggingInstance.filterByCategories(setOf(MessageCategory(1, "Category 1"), MessageCategory(2, "Category 2"))) }
    }

    @Test
    fun filterByCategories_shouldCallMethod_onActiveInstance_activeSDK() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Active)
        embeddedMessaging.registerOnContext()

        val categories = listOf(MessageCategory(1, "Category 1"), MessageCategory(2, "Category 2"))
        embeddedMessaging.filterByCategories(categories)

        verify { mockInternalInstance.filterByCategories(setOf(MessageCategory(1, "Category 1"), MessageCategory(2, "Category 2"))) }
    }
}