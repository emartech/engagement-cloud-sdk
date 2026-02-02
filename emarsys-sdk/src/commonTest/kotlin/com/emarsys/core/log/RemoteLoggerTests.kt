package com.emarsys.core.log

import com.emarsys.context.SdkContextApi
import com.emarsys.event.SdkEvent
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture.Companion.slot
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RemoteLoggerTests {

    private lateinit var mockLogEventRegistry: LogEventRegistryApi
    private lateinit var remoteLogger: RemoteLoggerApi
    private lateinit var mockSdkContext: SdkContextApi

    @BeforeTest
    fun setup() {
        mockLogEventRegistry = mock(MockMode.autofill)
        mockSdkContext = mock(MockMode.autofill)
        remoteLogger = RemoteLogger(mockLogEventRegistry, mockSdkContext)
    }

    @Test
    fun testLogToRemote_whenRemoteLogLevelIsLowerThanLogLevel() = runTest {
        val logLevel = LogLevel.Info
        every { mockSdkContext.remoteLogLevel } returns LogLevel.Debug
        val logMessage = JsonObject(mapOf("message" to JsonPrimitive("This is a test log")))
        everySuspend { mockLogEventRegistry.registerLogEvent(any()) }  returns Unit

        remoteLogger.logToRemote(logLevel, logMessage)

        verifySuspend(VerifyMode.exactly(0)) {
            mockLogEventRegistry.registerLogEvent(any())
        }
    }

    @Test
    fun testLogToRemote_whenRemoteLogLevelIsHigher() = runTest {
        val logLevel = LogLevel.Info
        every { mockSdkContext.remoteLogLevel } returns LogLevel.Error

        val logMessage = JsonObject(mapOf("message" to JsonPrimitive("This is a test log")))
        val eventCapture = slot<SdkEvent.Internal.Sdk.Log>()
        everySuspend { mockLogEventRegistry.registerLogEvent(capture(eventCapture)) }  returns Unit

        remoteLogger.logToRemote(logLevel, logMessage)

        verifySuspend {
            mockLogEventRegistry.registerLogEvent(any())
        }
        val capturedEvent: SdkEvent.Internal.Sdk.Log = eventCapture.get()
        capturedEvent.level shouldBe logLevel
        capturedEvent.name shouldBe "log"
        capturedEvent.attributes shouldBe logMessage
    }

    @Test
    fun testLogToRemote_whenRemoteLogLevelEqualsLogLevel() = runTest {
        val logLevel = LogLevel.Info
        every { mockSdkContext.remoteLogLevel } returns LogLevel.Info

        val logMessage = JsonObject(mapOf("message" to JsonPrimitive("This is a test log")))
        val eventCapture = slot<SdkEvent.Internal.Sdk.Log>()
        everySuspend { mockLogEventRegistry.registerLogEvent(capture(eventCapture)) } returns Unit

        remoteLogger.logToRemote(logLevel, logMessage)

        verifySuspend {
            mockLogEventRegistry.registerLogEvent(any())
        }
        val capturedEvent: SdkEvent.Internal.Sdk.Log = eventCapture.get()
        capturedEvent.level shouldBe logLevel
        capturedEvent.name shouldBe "log"
        capturedEvent.attributes shouldBe logMessage
    }
}