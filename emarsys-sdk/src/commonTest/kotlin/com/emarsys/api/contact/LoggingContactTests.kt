package com.emarsys.api.contact

import com.emarsys.core.log.Logger
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class LoggingContactTests {
    companion object {
        const val CONTACT_FIELD_ID = 42
        const val CONTACT_FIELD_VALUE = "testContactFieldValue"
        const val OPEN_ID_TOKEN = "testOpenIdToken"
    }

    private lateinit var mockSdkLogger: Logger
    private lateinit var loggingContact: LoggingContact

    @BeforeTest
    fun setup() = runTest {
        mockSdkLogger = mock()
        everySuspend { mockSdkLogger.debug(any()) } returns Unit

        loggingContact = LoggingContact(mockSdkLogger)
    }

    @Test
    fun testLinkContact() = runTest {
        loggingContact.linkContact(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE)

        verifyLogging()
    }

    @Test
    fun testLinkAuthenticatedContact() = runTest {
        loggingContact.linkAuthenticatedContact(CONTACT_FIELD_ID, OPEN_ID_TOKEN)

        verifyLogging()
    }

    @Test
    fun testUnlinkContact() = runTest {
        loggingContact.linkAuthenticatedContact(CONTACT_FIELD_ID, OPEN_ID_TOKEN)

        verifyLogging()
    }

    @Test
    fun testActive() = runTest {
        loggingContact.linkAuthenticatedContact(CONTACT_FIELD_ID, OPEN_ID_TOKEN)

        verifyLogging()
    }

    private fun verifyLogging() {
        //TODO: figure out argument capturing
//        val slot = Capture.slot<LogEntry>()
//        verifySuspend { mockSdkLogger.debug(capture(slot)) }
//
//        val capturedLogEntry = slot.get()
//        capturedLogEntry.topic shouldBe "log_method_not_allowed"
    }
}