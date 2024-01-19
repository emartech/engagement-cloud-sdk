package com.emarsys.api.contact

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class ContactGathererTests {

    companion object {
        val contactFieldId = 42
        val contactFieldValue = "testContactFieldValue"
        val openIdToken = "testOpenIdToken"
    }

    private lateinit var contactContext: ContactContext
    private lateinit var contactGatherer: ContactGatherer

    @BeforeTest
    fun setup() {
        contactContext = ContactContext()
        contactGatherer = ContactGatherer(contactContext)
    }

    @Test
    fun testGathering() = runTest {
        val linkContact = ContactCall.LinkContact(contactFieldId, contactFieldValue)
        val linkAuthenticatedContact = ContactCall.LinkAuthenticatedContact(contactFieldId, openIdToken)
        val unlinkContact = ContactCall.UnlinkContact()

        val expected = listOf(
            linkContact,
            linkAuthenticatedContact,
            unlinkContact,
            linkContact,
            unlinkContact,
            linkAuthenticatedContact
        )

        contactGatherer.linkContact(contactFieldId, contactFieldValue)
        contactGatherer.linkAuthenticatedContact(contactFieldId, openIdToken)
        contactGatherer.unlinkContact()
        contactGatherer.linkContact(contactFieldId, contactFieldValue)
        contactGatherer.unlinkContact()
        contactGatherer.linkAuthenticatedContact(contactFieldId, openIdToken)

        contactContext.calls shouldBe expected
    }

}
