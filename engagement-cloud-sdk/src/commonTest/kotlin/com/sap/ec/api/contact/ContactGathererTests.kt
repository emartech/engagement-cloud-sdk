package com.sap.ec.api.contact

import com.sap.ec.TestEngagementCloudSDKConfig
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.collections.ThreadSafePersistentStore
import com.sap.ec.core.collections.ThreadSafePersistentStoreApi
import com.sap.ec.core.storage.StorageApi
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class ContactGathererTests {
    private companion object {
        const val TEST_STORE_ID = "testStoreId"
        const val CONTACT_FIELD_VALUE = "testContactFieldValue"
        const val OPEN_ID_TOKEN = "testOpenIdToken"
        const val APPLICATION_CODE = "testAppCode"
        val linkContact = ContactCall.LinkContact(CONTACT_FIELD_VALUE)
        val linkAuthenticatedContact =
            ContactCall.LinkAuthenticatedContact(OPEN_ID_TOKEN)
        val unlinkContact = ContactCall.UnlinkContact(APPLICATION_CODE)

        val expected = mutableListOf(
            linkContact,
            linkAuthenticatedContact,
            unlinkContact,
            linkContact,
            unlinkContact,
            linkAuthenticatedContact
        )
    }

    private lateinit var contactGatherer: ContactGatherer
    private lateinit var mockStorage: StorageApi
    private lateinit var threadSafePersistentStore: ThreadSafePersistentStoreApi<ContactCall>
    private lateinit var mockSdkContext: SdkContextApi

    @BeforeTest
    fun setup() {
        mockStorage = mock(MockMode.autofill)
        threadSafePersistentStore = ThreadSafePersistentStore(
            TEST_STORE_ID,
            mockStorage,
            ContactCall.serializer()
        )
        mockSdkContext = mock(MockMode.autofill)
        everySuspend { mockSdkContext.getSdkConfig() } returns TestEngagementCloudSDKConfig(
            APPLICATION_CODE
        )
        contactGatherer =
            ContactGatherer(
                mockSdkContext,
                threadSafePersistentStore,
                sdkLogger = mock(MockMode.autofill)
            )
    }

    @Test
    fun testGathering() = runTest {
        contactGatherer.link(CONTACT_FIELD_VALUE)
        contactGatherer.linkAuthenticated(OPEN_ID_TOKEN)
        contactGatherer.unlink()
        contactGatherer.link(CONTACT_FIELD_VALUE)
        contactGatherer.unlink()
        contactGatherer.linkAuthenticated(OPEN_ID_TOKEN)

        threadSafePersistentStore.items shouldBe expected
    }

}
