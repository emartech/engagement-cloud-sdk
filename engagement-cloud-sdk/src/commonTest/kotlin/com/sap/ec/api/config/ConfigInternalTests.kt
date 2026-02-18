package com.sap.ec.api.config

import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.exceptions.SdkException
import com.sap.ec.core.language.LanguageHandlerApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.providers.InstantProvider
import com.sap.ec.core.providers.UuidProviderApi
import com.sap.ec.event.SdkEvent
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class ConfigInternalTests {
    private companion object {
        const val UUID = "testUUID"
        val TIMESTAMP = Clock.System.now()
        const val APPCODE = "A1s2D-F3G4H"
        const val MULTI_REGION_APPCODE = "INS-S01-APP-ABC12"
        const val HUNGARIAN_LANGUAGE = "hu-HU"
    }

    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi
    private lateinit var mockUuidProvider: UuidProviderApi
    private lateinit var mockTimestampProvider: InstantProvider
    private lateinit var mockLogger: Logger
    private lateinit var mockLanguageHandler: LanguageHandlerApi
    private lateinit var configInternal: ConfigInternal
    private lateinit var mockConfigContext: ConfigContextApi

    @BeforeTest
    fun setUp() = runTest {
        mockLogger = mock()
        everySuspend { mockLogger.debug(message = any()) } returns Unit
        mockUuidProvider = mock()
        every { mockUuidProvider.provide() } returns UUID
        mockTimestampProvider = mock()
        every { mockTimestampProvider.provide() } returns TIMESTAMP
        mockLanguageHandler = mock(MockMode.autofill)
        mockSdkEventDistributor = mock(MockMode.autofill)
        everySuspend { mockSdkEventDistributor.registerEvent(any()) } returns mock(MockMode.autofill)
        mockConfigContext = mock()
        configInternal =
            ConfigInternal(
                mockSdkEventDistributor,
                mockConfigContext,
                mockUuidProvider,
                mockTimestampProvider,
                mockLogger,
                mockLanguageHandler,
            )
    }

    @Test
    fun testChangeApplicationCode_shouldEmitChangeApplicationCodeEvent_toSdkEventFlow() = runTest {
        val expectedEvent = SdkEvent.Internal.Sdk.ChangeAppCode(
            id = UUID,
            applicationCode = APPCODE.uppercase(),
            timestamp = TIMESTAMP
        )

        configInternal.changeApplicationCode(APPCODE)

        verifySuspend { mockSdkEventDistributor.registerEvent(expectedEvent) }
    }

    @Test
    fun testChangeApplicationCode_shouldEmitChangeApplicationCodeEvent_toSdkEventFlow_whenMultiRegionAppCode_isUsed() = runTest {
        val expectedEvent = SdkEvent.Internal.Sdk.ChangeAppCode(
            id = UUID,
            applicationCode = MULTI_REGION_APPCODE.uppercase(),
            timestamp = TIMESTAMP
        )

        configInternal.changeApplicationCode(MULTI_REGION_APPCODE)

        verifySuspend { mockSdkEventDistributor.registerEvent(expectedEvent) }
    }

    @Test
    fun testChangeApplicationCode_shouldReturn_whenAppCode_isEmpty() = runTest {
        everySuspend { mockLogger.error(message = any()) } returns Unit
        shouldThrow<SdkException.InvalidApplicationCodeException> {
            configInternal.changeApplicationCode(" ")
        }

        verifySuspend(mode = VerifyMode.exactly(0)) { mockSdkEventDistributor.registerEvent(any()) }
    }

    @Test
    fun testChangeApplicationCode_shouldThrow_whenAppCode_isInvalid() = runTest {
        forAll(
            row("EMS 11-C3FD3"),
            row("EMS"),
            row("-EMS11-C3FD3"),
            row("EMS11--C3FD3"),
            row("EMS11-C3FD3-"),
            row("EMS11-C3FD3-C3FD3"),
            row("INS-S01-APP-1234"),
            row("INS-S01-12345"),
            row("INS-APP-12345"),
            row("${MULTI_REGION_APPCODE}123"),
            row("EXTRA_START-${MULTI_REGION_APPCODE}"),
            row("${MULTI_REGION_APPCODE}-EXTRA_END"),
        ) { appCode ->
            everySuspend { mockLogger.error(any(), any<Throwable>(), true) } returns Unit
            shouldThrow<SdkException.InvalidApplicationCodeException> {
                configInternal.changeApplicationCode(appCode)
            }
        }
        verifySuspend(mode = VerifyMode.exactly(0)) { mockSdkEventDistributor.registerEvent(any()) }
    }

    @Test
    fun testSetLanguage_shouldCallHandleLanguage_onLanguageHandler() = runTest {
        configInternal.setLanguage(HUNGARIAN_LANGUAGE)

        verifySuspend {
            mockLanguageHandler.handleLanguage(HUNGARIAN_LANGUAGE)
        }
    }

    @Test
    fun testResetLanguage_shouldCallHandleLanguage_withNull_inLanguageHandler() = runTest {
        configInternal.resetLanguage()

        verifySuspend {
            mockLanguageHandler.handleLanguage(null)
        }
    }

    @Test
    fun testActivate_shouldReplayStoredCallsFromConfigContext_inTheRightOrder() = runTest {
        val slot = Capture.slot<SdkEvent>()
        val testCalls = mutableListOf(
            ConfigCall.ChangeApplicationCode(APPCODE),
            ConfigCall.SetLanguage(HUNGARIAN_LANGUAGE),
            ConfigCall.ResetLanguage
        )
        everySuspend { mockConfigContext.calls } returns testCalls
        everySuspend { mockSdkEventDistributor.registerEvent(capture(slot)) } returns mock()

        configInternal.activate()

        val testEvent = slot.get()
        verifySuspend(VerifyMode.order) {
            mockSdkEventDistributor.registerEvent(testEvent)
            testEvent shouldBe SdkEvent.Internal.Sdk.ChangeAppCode(
                id = UUID,
                applicationCode = APPCODE.uppercase(),
                timestamp = TIMESTAMP
            )
            mockLanguageHandler.handleLanguage(HUNGARIAN_LANGUAGE)
            mockLanguageHandler.handleLanguage(null)
        }
        testCalls.size shouldBe 0
    }

}