package com.emarsys.api.contact

import com.emarsys.core.log.LogEntry
import com.emarsys.core.log.LogLevel
import com.emarsys.core.log.Logger
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class FakeSdkLogger: Logger {

    var funCalls: MutableList<Pair<LogEntry, LogLevel>> = mutableListOf()
    override fun log(entry: LogEntry, level: LogLevel) {
        funCalls.add(Pair(entry, level))
    }

    override fun debug(entry: LogEntry) {
        log(entry, LogLevel.Debug)
    }

    override fun error(entry: LogEntry) {
        log(entry, LogLevel.Error)
    }

}

class LoggingContactTests {

    companion object {
        val contactFieldId = 42
        val contactFieldValue = "testContactFieldValue"
        val openIdToken = "testOpenIdToken"
    }

    lateinit var fakeLogger: FakeSdkLogger
    lateinit var loggingContact: LoggingContact

    @BeforeTest
    fun setup() = runTest {
        fakeLogger = FakeSdkLogger()
        loggingContact = LoggingContact(fakeLogger)
    }

    @Test
    fun testLinkContact() = runTest {
        loggingContact.linkContact(contactFieldId, contactFieldValue)

        fakeLogger.funCalls.first().first.topic shouldBe "log_method_not_allowed"
        fakeLogger.funCalls.first().second shouldBe LogLevel.Debug
    }

    @Test
    fun testLinkAuthenticatedContact() = runTest {
        loggingContact.linkAuthenticatedContact(contactFieldId, openIdToken)

        fakeLogger.funCalls.first().first.topic shouldBe "log_method_not_allowed"
        fakeLogger.funCalls.first().second shouldBe LogLevel.Debug
    }

    @Test
    fun testUnlinkContact() = runTest {
        loggingContact.linkAuthenticatedContact(contactFieldId, openIdToken)

        fakeLogger.funCalls.first().first.topic shouldBe "log_method_not_allowed"
        fakeLogger.funCalls.first().second shouldBe LogLevel.Debug
    }

    @Test
    fun testActive() = runTest {
        loggingContact.linkAuthenticatedContact(contactFieldId, openIdToken)

        fakeLogger.funCalls.first().first.topic shouldBe "log_method_not_allowed"
        fakeLogger.funCalls.first().second shouldBe LogLevel.Debug
    }

}