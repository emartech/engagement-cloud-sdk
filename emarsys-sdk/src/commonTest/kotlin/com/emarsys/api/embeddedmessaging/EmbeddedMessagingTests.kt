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
    fun activeCategoryIdFilters_shouldGetValue_fromActiveInstance_initializedSDK() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Initialized)
        every { mockLoggingInstance.activeCategoryIdFilters } returns emptySet()

        embeddedMessaging.registerOnContext()

        embeddedMessaging.activeCategoryIdFilters shouldBe emptyList()
        verify { mockLoggingInstance.activeCategoryIdFilters }
    }

    @Test
    fun activeCategoryIdFilters_shouldGetValue_fromActiveInstance_activeSDK() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Active)
        every { mockInternalInstance.activeCategoryIdFilters } returns emptySet()

        embeddedMessaging.registerOnContext()

        embeddedMessaging.activeCategoryIdFilters shouldBe emptyList()
        verify { mockInternalInstance.activeCategoryIdFilters }
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
    fun filterByCategoryIds_shouldCallMethod_onActiveInstance_initializedSDK() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Initialized)
        embeddedMessaging.registerOnContext()

        embeddedMessaging.filterByCategoryIds(listOf(1, 2))

        verify { mockLoggingInstance.filterByCategoryIds(setOf(1, 2)) }
    }

    @Test
    fun filterByCategoryIds_shouldCallMethod_onActiveInstance_activeSDK() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Active)
        embeddedMessaging.registerOnContext()

        embeddedMessaging.filterByCategoryIds(listOf(1, 2))

        verify { mockInternalInstance.filterByCategoryIds(setOf(1, 2)) }
    }
}