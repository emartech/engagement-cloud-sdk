package com.emarsys.api.contact

import dev.mokkery.MockMode
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class ContactGathererTests {

    private companion object {
        const val contactFieldId = 42
        const val contactFieldValue = "testContactFieldValue"
        const val openIdToken = "testOpenIdToken"
        val linkContact = ContactCall.LinkContact(contactFieldId, contactFieldValue)
        val linkAuthenticatedContact =
            ContactCall.LinkAuthenticatedContact(contactFieldId, openIdToken)
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

    private lateinit var contactContext: ContactContext
    private lateinit var contactGatherer: ContactGatherer

    @BeforeTest
    fun setup() {
        contactContext = ContactContext(expected)
        contactGatherer = ContactGatherer(contactContext, sdkLogger = mock(MockMode.autofill))
    }

    @Test
    fun testGathering() = runTest {

        contactGatherer.linkContact(contactFieldId, contactFieldValue)
        contactGatherer.linkAuthenticatedContact(contactFieldId, openIdToken)
        contactGatherer.unlinkContact()
        contactGatherer.linkContact(contactFieldId, contactFieldValue)
        contactGatherer.unlinkContact()
        contactGatherer.linkAuthenticatedContact(contactFieldId, openIdToken)

        contactContext.calls shouldBe expected
    }

}
