package com.emarsys.api.generic

import com.emarsys.api.SdkState.active
import com.emarsys.api.SdkState.inactive
import com.emarsys.api.SdkState.onHold
import com.emarsys.api.contact.ContactContext
import com.emarsys.api.contact.ContactInternal
import com.emarsys.api.contact.FakeSdkLogger
import com.emarsys.api.contact.ContactGatherer
import com.emarsys.api.contact.LoggingContact
import com.emarsys.context.SdkContext
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.isActive
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class GenericApiTests {

    private lateinit var loggingContact: LoggingContact
    private lateinit var contactGatherer: ContactGatherer
    private lateinit var contactInternal: ContactInternal
    private lateinit var sdkContext: SdkContext
    private lateinit var genericApi: GenericApi<LoggingContact, ContactGatherer, ContactInternal>

    @BeforeTest
    fun setup() = runTest {
        loggingContact = LoggingContact(FakeSdkLogger())
        contactGatherer = ContactGatherer(ContactContext())
        contactInternal = ContactInternal()
        sdkContext = SdkContext(StandardTestDispatcher())
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