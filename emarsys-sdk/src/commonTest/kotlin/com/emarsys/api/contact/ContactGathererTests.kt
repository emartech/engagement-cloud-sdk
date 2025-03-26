package com.emarsys.api.contact

import dev.mokkery.MockMode
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class ContactGathererTests {
    private companion object {
        const val CONTACT_FIELD_ID = 42
        const val CONTACT_FIELD_VALUE = "testContactFieldValue"
        const val OPEN_ID_TOKEN = "testOpenIdToken"
        val linkContact = ContactCall.LinkContact(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE)
        val linkAuthenticatedContact =
            ContactCall.LinkAuthenticatedContact(CONTACT_FIELD_ID, OPEN_ID_TOKEN)
        val unlinkContact = ContactCall.UnlinkContact()

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
    private lateinit var contactContext: ContactContextApi

    @BeforeTest
    fun setup() {
        contactContext = ContactContext(mutableListOf())
        contactGatherer = ContactGatherer(contactContext, sdkLogger = mock(MockMode.autofill))
    }

    @Test
    fun testGathering() = runTest {

        contactGatherer.linkContact(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE)
        contactGatherer.linkAuthenticatedContact(CONTACT_FIELD_ID, OPEN_ID_TOKEN)
        contactGatherer.unlinkContact()
        contactGatherer.linkContact(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE)
        contactGatherer.unlinkContact()
        contactGatherer.linkAuthenticatedContact(CONTACT_FIELD_ID, OPEN_ID_TOKEN)

        contactContext.calls shouldBe expected
    }

}
