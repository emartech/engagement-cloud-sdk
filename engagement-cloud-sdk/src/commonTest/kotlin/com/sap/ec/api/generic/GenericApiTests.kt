package com.sap.ec.api.generic

import com.sap.ec.api.SdkState.Active
import com.sap.ec.api.SdkState.Initialized
import com.sap.ec.api.SdkState.OnHold
import com.sap.ec.api.SdkState.UnInitialized
import com.sap.ec.api.contact.ContactContext
import com.sap.ec.api.contact.ContactContextApi
import com.sap.ec.api.contact.ContactGatherer
import com.sap.ec.api.contact.ContactInternal
import com.sap.ec.api.contact.LoggingContact
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.context.RequestContextApi
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class GenericApiTests {
    private lateinit var mockSdkLogger: Logger
    private lateinit var loggingContact: LoggingContact
    private lateinit var contactGatherer: ContactGatherer
    private lateinit var contactInternal: ContactInternal
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var contactContext: ContactContextApi
    private lateinit var genericApi: GenericApi<LoggingContact, ContactGatherer, ContactInternal>
    private lateinit var mockRequestContext: RequestContextApi

    @BeforeTest
    fun setup() {
        val mainDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(mainDispatcher)
        contactContext = ContactContext(mutableListOf())
        mockSdkContext = mock(MockMode.autofill)
        mockRequestContext = mock(MockMode.autofill)
        mockSdkLogger = mock(MockMode.autofill)
        loggingContact = LoggingContact(mockSdkLogger)
        contactGatherer = ContactGatherer(contactContext, mockSdkContext, mockSdkLogger)
        contactInternal =
            ContactInternal(
                contactContext,
                mockSdkLogger,
                sdkEventDistributor = mock(),
                mockSdkContext,
                mockRequestContext
            )
        every { mockSdkContext.sdkDispatcher } returns mainDispatcher
        genericApi = GenericApi(
            loggingContact,
            contactGatherer,
            contactInternal,
            mockSdkContext
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testActive_whenSdkState_isUnInitialized() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(UnInitialized)
        genericApi.registerOnContext()

        advanceUntilIdle()

        genericApi.activeInstance shouldBe loggingContact
    }

    @Test
    fun testActive_whenSdkState_isOnHold() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(OnHold)
        genericApi.registerOnContext()

        advanceUntilIdle()

        genericApi.activeInstance shouldBe contactGatherer
    }

    @Test
    fun testActive_whenSdkState_isActive() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(Active)
        genericApi.registerOnContext()

        advanceUntilIdle()

        genericApi.activeInstance shouldBe contactInternal
    }

    @Test
    fun testActive_whenSdkState_isInitialized() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(Initialized)
        genericApi.registerOnContext()

        advanceUntilIdle()

        genericApi.activeInstance shouldBe loggingContact
    }
}