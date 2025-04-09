package com.emarsys.api.generic

import com.emarsys.api.SdkState.active
import com.emarsys.api.SdkState.inactive
import com.emarsys.api.SdkState.initialized
import com.emarsys.api.SdkState.onHold
import com.emarsys.api.contact.ContactContext
import com.emarsys.api.contact.ContactContextApi
import com.emarsys.api.contact.ContactGatherer
import com.emarsys.api.contact.ContactInternal
import com.emarsys.api.contact.LoggingContact
import com.emarsys.context.DefaultUrls
import com.emarsys.context.SdkContext
import com.emarsys.core.log.LogLevel
import com.emarsys.core.log.Logger
import dev.mokkery.MockMode
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class GenericApiTests {
    private lateinit var mockSdkLogger: Logger
    private lateinit var loggingContact: LoggingContact
    private lateinit var contactGatherer: ContactGatherer
    private lateinit var contactInternal: ContactInternal
    private lateinit var sdkContext: SdkContext
    private lateinit var contactContext: ContactContextApi
    private lateinit var genericApi: GenericApi<LoggingContact, ContactGatherer, ContactInternal>

    @BeforeTest
    fun setup() = runTest {
        val mainDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(mainDispatcher)
        contactContext = ContactContext(mutableListOf())
        mockSdkLogger = mock(MockMode.autofill)
        loggingContact = LoggingContact(mockSdkLogger)
        contactGatherer = ContactGatherer(contactContext, mockSdkLogger)
        contactInternal =
            ContactInternal(contactContext, mockSdkLogger, sdkEventDistributor = mock())
        sdkContext = SdkContext(
            StandardTestDispatcher(),
            mainDispatcher,
            DefaultUrls(
                "",
                "",
                "",
                "",
                "",
                "",
                ""
            ),
            LogLevel.Error,
            mutableSetOf(),
            logBreadcrumbsQueueSize = 10
        )
        genericApi = GenericApi(loggingContact, contactGatherer, contactInternal, sdkContext)
        genericApi.registerOnContext()
    }

    @Test
    fun testActive_whenSdkState_isInactive() = runTest {
        sdkContext.setSdkState(inactive)

        advanceUntilIdle()

        genericApi.activeInstance shouldBe loggingContact
    }

    @Test
    fun testActive_whenSdkState_isOnHold() = runTest {
        sdkContext.setSdkState(onHold)

        advanceUntilIdle()

        genericApi.activeInstance shouldBe contactGatherer
    }

    @Test
    fun testActive_whenSdkState_isActive() = runTest {
        sdkContext.setSdkState(active)

        advanceUntilIdle()

        genericApi.activeInstance shouldBe contactInternal
    }

    @Test
    fun testActive_whenSdkState_isInitialized() = runTest {
        sdkContext.setSdkState(initialized)

        advanceUntilIdle()

        genericApi.activeInstance shouldBe loggingContact
    }
}