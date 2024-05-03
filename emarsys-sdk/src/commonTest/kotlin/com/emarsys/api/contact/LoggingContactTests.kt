package com.emarsys.api.contact

import com.emarsys.core.log.LogEntry
import com.emarsys.core.log.Logger
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.BeforeTest
import kotlin.test.Test

class LoggingContactTests : TestsWithMocks() {
    companion object {
        val contactFieldId = 42
        val contactFieldValue = "testContactFieldValue"
        val openIdToken = "testOpenIdToken"
    }

    override fun setUpMocks() = injectMocks(mocker)

    @Mock
    lateinit var mockSdkLogger: Logger

    private lateinit var loggingContact: LoggingContact

    @BeforeTest
    fun setup() = runTest {
        everySuspending {  mockSdkLogger.debug(isAny()) } returns Unit

        loggingContact = LoggingContact(mockSdkLogger)
    }

    @Test
    fun testLinkContact() = runTest {
        loggingContact.linkContact(contactFieldId, contactFieldValue)

        verifyLogging()
    }

    @Test
    fun testLinkAuthenticatedContact() = runTest {
        loggingContact.linkAuthenticatedContact(contactFieldId, openIdToken)

        verifyLogging()
    }

    @Test
    fun testUnlinkContact() = runTest {
        loggingContact.linkAuthenticatedContact(contactFieldId, openIdToken)

        verifyLogging()
    }

    @Test
    fun testActive() = runTest {
        loggingContact.linkAuthenticatedContact(contactFieldId, openIdToken)

        verifyLogging()
    }

    private suspend fun verifyLogging() {
        val logEntryCapture = mutableListOf<LogEntry>()
        verifyWithSuspend { mockSdkLogger.debug(isAny(capture = logEntryCapture)) }
        logEntryCapture.first().topic shouldBe "log_method_not_allowed"
    }
}