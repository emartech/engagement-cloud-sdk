package com.sap.ec.api.contact

import dev.mokkery.MockMode
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class ContactGathererTests {
    private companion object {
        const val CONTACT_FIELD_VALUE = "testContactFieldValue"
        const val OPEN_ID_TOKEN = "testOpenIdToken"
        val linkContact = ContactCall.LinkContact(CONTACT_FIELD_VALUE)
        val linkAuthenticatedContact =
            ContactCall.LinkAuthenticatedContact(OPEN_ID_TOKEN)
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

        contactGatherer.link(CONTACT_FIELD_VALUE)
        contactGatherer.linkAuthenticated(OPEN_ID_TOKEN)
        contactGatherer.unlink()
        contactGatherer.link(CONTACT_FIELD_VALUE)
        contactGatherer.unlink()
        contactGatherer.linkAuthenticated(OPEN_ID_TOKEN)

        contactContext.calls shouldBe expected
    }

}
