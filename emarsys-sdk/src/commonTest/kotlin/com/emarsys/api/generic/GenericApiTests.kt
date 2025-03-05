package com.emarsys.api.generic

import com.emarsys.api.SdkState.active
import com.emarsys.api.SdkState.inactive
import com.emarsys.api.SdkState.onHold
import com.emarsys.api.contact.ContactCall
import com.emarsys.api.contact.ContactContext
import com.emarsys.api.contact.ContactGatherer
import com.emarsys.api.contact.ContactInternal
import com.emarsys.api.contact.LoggingContact
import com.emarsys.context.DefaultUrls
import com.emarsys.context.SdkContext
import com.emarsys.core.log.LogLevel
import com.emarsys.core.log.Logger
import com.emarsys.networking.clients.contact.ContactClientApi
import dev.mokkery.MockMode
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class GenericApiTests {
    private lateinit var mockContactClient: ContactClientApi
    private lateinit var mockSdkLogger: Logger
    private lateinit var loggingContact: LoggingContact
    private lateinit var contactGatherer: ContactGatherer
    private lateinit var contactInternal: ContactInternal
    private lateinit var sdkContext: SdkContext
    private lateinit var genericApi: GenericApi<LoggingContact, ContactGatherer, ContactInternal>
    private lateinit var contactContext: ApiContext<ContactCall>

    private val mainDispatcher = StandardTestDispatcher()

    init {
        Dispatchers.setMain(mainDispatcher)
    }

    @BeforeTest
    fun setup() = runTest {
        mockContactClient = mock()
        mockSdkLogger = mock(MockMode.autofill)
        contactContext = ContactContext(mutableListOf())
        loggingContact = LoggingContact(mockSdkLogger)
        contactGatherer = ContactGatherer(contactContext, mockSdkLogger)
        contactInternal = ContactInternal(mockContactClient, contactContext, mockSdkLogger)
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
            mutableSetOf()
        )
        genericApi = GenericApi(loggingContact, contactGatherer, contactInternal, sdkContext)
    }

    @Test
    fun testActive_whenSdkState_isInactive() = runTest {
        sdkContext.setSdkState(inactive)

        while (!sdkContext.sdkDispatcher.isActive)

            genericApi.activeInstance shouldBe loggingContact
    }

    @Test
    fun testActive_whenSdkState_isOnHold() = runTest {
        sdkContext.setSdkState(onHold)

        while (!sdkContext.sdkDispatcher.isActive)

            genericApi.activeInstance shouldBe contactGatherer
    }

    @Test
    fun testActive_whenSdkState_isActive() = runTest {
        sdkContext.setSdkState(active)

        while (!sdkContext.sdkDispatcher.isActive)

            genericApi.activeInstance shouldBe contactInternal
    }
}