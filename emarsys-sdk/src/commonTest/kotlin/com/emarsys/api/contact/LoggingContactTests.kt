package com.emarsys.api.contact

import com.emarsys.core.log.LogEntry
import com.emarsys.core.log.Logger
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
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
    private var slot = Capture.slot<LogEntry>()

    @BeforeTest
    fun setup() = runTest {
        mockSdkLogger = mock(MockMode.autofill)
        everySuspend { mockSdkLogger.debug(capture<LogEntry>(slot)) } returns Unit

        loggingContact = LoggingContact(mockSdkLogger)
    }

    @Test
    fun testLinkContact() = runTest {
        loggingContact.link(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE)

        verifyLogging()
    }

    @Test
    fun testLinkAuthenticatedContact() = runTest {
        loggingContact.linkAuthenticated(CONTACT_FIELD_ID, OPEN_ID_TOKEN)

        verifyLogging()
    }

    @Test
    fun testUnlinkContact() = runTest {
        loggingContact.linkAuthenticated(CONTACT_FIELD_ID, OPEN_ID_TOKEN)

        verifyLogging()
    }

    @Test
    fun testActive() = runTest {
        loggingContact.activate()

        verifyLogging()
    }

    private fun verifyLogging() {
        val capturedLogEntry = slot.get()
        capturedLogEntry.topic shouldBe "log_method_not_allowed"
    }
}