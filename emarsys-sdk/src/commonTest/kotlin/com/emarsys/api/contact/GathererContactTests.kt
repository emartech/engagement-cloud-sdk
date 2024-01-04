package com.emarsys.api.contact

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class GathererContactTests {

    companion object {
        val contactFieldId = 42
        val contactFieldValue = "testContactFieldValue"
        val openIdToken = "testOpenIdToken"
    }

    lateinit private var contactContext: ContactContext
    lateinit private var gathererContact: GathererContact

    @BeforeTest
    fun setup() {
        contactContext = ContactContext()
        gathererContact = GathererContact(contactContext)
    }

    @Test
    fun testGathering() = runTest {
        val linkContact = ContactCall.LinkContact(contactFieldId, contactFieldValue)
        val linkAuthenticatedContact = ContactCall.LinkAuthenticatedContact(contactFieldId, openIdToken)
        val unlinkContact = ContactCall.UnlinkContact()

        val expected = listOf(linkContact, linkAuthenticatedContact, unlinkContact, linkContact, unlinkContact, linkAuthenticatedContact)

        gathererContact.linkContact(contactFieldId, contactFieldValue)
        gathererContact.linkAuthenticatedContact(contactFieldId, openIdToken)
        gathererContact.unlinkContact()
        gathererContact.linkContact(contactFieldId, contactFieldValue)
        gathererContact.unlinkContact()
        gathererContact.linkAuthenticatedContact(contactFieldId, openIdToken)

        contactContext.calls shouldBe expected
    }

}
