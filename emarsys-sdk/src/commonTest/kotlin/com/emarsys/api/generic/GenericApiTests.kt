package com.emarsys.api.generic

import com.emarsys.api.SdkState.active
import com.emarsys.api.SdkState.inactive
import com.emarsys.api.SdkState.onHold
import com.emarsys.api.contact.ContactContext
import com.emarsys.api.contact.ContactGatherer
import com.emarsys.api.contact.ContactInternal
import com.emarsys.api.contact.FakeSdkLogger
import com.emarsys.api.contact.LoggingContact
import com.emarsys.context.DefaultUrls
import com.emarsys.context.SdkContext
import com.emarsys.core.log.LogLevel
import com.emarsys.networking.clients.contact.ContactClientApi
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.isActive
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.BeforeTest
import kotlin.test.Test

class GenericApiTests: TestsWithMocks() {

    override fun setUpMocks() = injectMocks(mocker)

    @Mock
    lateinit var mockContactClient: ContactClientApi

    private lateinit var loggingContact: LoggingContact
    private lateinit var contactGatherer: ContactGatherer
    private lateinit var contactInternal: ContactInternal
    private lateinit var sdkContext: SdkContext
    private lateinit var genericApi: GenericApi<LoggingContact, ContactGatherer, ContactInternal>

    @BeforeTest
    fun setup() = runTest {
        loggingContact = LoggingContact(FakeSdkLogger())
        contactGatherer = ContactGatherer(ContactContext(mutableListOf()))
        contactInternal = ContactInternal(mockContactClient)
        sdkContext = SdkContext(StandardTestDispatcher(), DefaultUrls("", "", "", "", "", "", ""), LogLevel.error, mutableSetOf())
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