package com.emarsys.core.session

import com.emarsys.core.session.SessionContext
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SessionContextTests {

    companion object {
        const val CONTACT_TOKEN = "testContactToken"
        const val OPEN_ID_TOKEN = "testOpenIdToken"
        const val CONTACT_FIELD_VALUE = "testContactFieldValue"
    }

    @Test
    fun testHasContactIdentification_when_contactTokenIsAvailable_should_returnTrue() {
        SessionContext(contactToken = CONTACT_TOKEN).hasContactIdentification() shouldBe true
    }

    @Test
    fun testHasContactIdentification_when_openIdTokenIsAvailable_should_returnTrue() {
        SessionContext(openIdToken = OPEN_ID_TOKEN).hasContactIdentification() shouldBe true
    }

    @Test
    fun testHasContactIdentification_when_contactFieldValueIsAvailable_should_returnTrue() {
        SessionContext(contactFieldValue = CONTACT_FIELD_VALUE).hasContactIdentification() shouldBe true
    }

    @Test
    fun testHasContactIdentification_when_contactTokenAndOpenIdTokenIsAvailable_should_returnTrue() {
        SessionContext(
            contactToken = CONTACT_TOKEN,
            openIdToken = OPEN_ID_TOKEN
        ).hasContactIdentification() shouldBe true
    }

    @Test
    fun testHasContactIdentification_when_contactTokenAndContactFieldValueIsAvailable_should_returnTrue() {
        SessionContext(
            contactToken = CONTACT_TOKEN,
            contactFieldValue = CONTACT_FIELD_VALUE
        ).hasContactIdentification() shouldBe true
    }

    @Test
    fun testHasContactIdentification_when_openIdTokenAndContactFieldValueIsAvailable_should_returnTrue() {
        SessionContext(
            openIdToken = OPEN_ID_TOKEN,
            contactFieldValue = CONTACT_FIELD_VALUE
        ).hasContactIdentification() shouldBe true
    }

    @Test
    fun testHasContactIdentification_when_noContactDataIsAvailable_should_returnFalse() {
        SessionContext().hasContactIdentification() shouldBe false
    }
}