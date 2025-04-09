package com.emarsys.core.log

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.di.SdkKoinIsolationContext.koin
import com.emarsys.networking.clients.event.model.SdkEvent
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture.Companion.slot
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.test.KoinTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class RemoteLoggerTests : KoinTest {

    override fun getKoin(): Koin = koin

    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi
    private lateinit var remoteLogger: RemoteLoggerApi
    private lateinit var testModule: Module

    @BeforeTest
    fun setup() {
        mockSdkEventDistributor = mock(MockMode.autofill)
        testModule = module {
            single<SdkEventDistributorApi> { mockSdkEventDistributor }
        }
        startKoin {
            modules(testModule)
        }
        remoteLogger = RemoteLogger()
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun testLogToRemote() = runTest {
        val logLevel = LogLevel.Info
        val logMessage = JsonObject(mapOf("message" to JsonPrimitive("This is a test log")))
        val eventCapture = slot<SdkEvent.Internal.Sdk.Log>()
        everySuspend { mockSdkEventDistributor.registerAndStoreLogEvent(capture(eventCapture)) } returns Unit

        remoteLogger.logToRemote(logLevel, logMessage)

        verifySuspend {
            mockSdkEventDistributor.registerAndStoreLogEvent(any())
        }
        val capturedEvent: SdkEvent.Internal.Sdk.Log = eventCapture.get()
        capturedEvent.level shouldBe logLevel
        capturedEvent.name shouldBe "log"
        capturedEvent.attributes shouldBe logMessage
    }
}