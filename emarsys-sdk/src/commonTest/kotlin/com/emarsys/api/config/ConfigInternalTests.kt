package com.emarsys.api.config

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.exceptions.SdkException
import com.emarsys.core.language.LanguageHandlerApi
import com.emarsys.core.log.Logger
import com.emarsys.core.providers.InstantProvider
import com.emarsys.core.providers.UuidProviderApi
import com.emarsys.networking.clients.event.model.SdkEvent
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.data.forAll
import io.kotest.data.row
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.BeforeTest
import kotlin.test.Test

class ConfigInternalTests {
    private companion object {
        const val UUID = "testUUID"
        val TIMESTAMP = Clock.System.now()
        const val APPCODE = "A1s2D-F3G4H"
        const val MERCHANT_ID = "testMerchantId"
    }

    private lateinit var sdkEventDistributor: SdkEventDistributorApi
    private lateinit var mockUuidProvider: UuidProviderApi
    private lateinit var mockTimestampProvider: InstantProvider
    private lateinit var mockLogger: Logger
    private lateinit var mockLanguageHandler: LanguageHandlerApi
    private lateinit var configInternal: ConfigInternal

    @BeforeTest
    fun setUp() = runTest {
        mockLogger = mock()
        everySuspend { mockLogger.debug(message = any()) } returns Unit
        mockUuidProvider = mock()
        every { mockUuidProvider.provide() } returns UUID
        mockTimestampProvider = mock()
        every { mockTimestampProvider.provide() } returns TIMESTAMP
        mockLanguageHandler = mock()
        sdkEventDistributor = mock(MockMode.autofill)
        everySuspend { sdkEventDistributor.registerEvent(any()) } returns mock(MockMode.autofill)
        configInternal =
            ConfigInternal(
                sdkEventDistributor,
                mockUuidProvider,
                mockTimestampProvider,
                mockLogger,
                mockLanguageHandler
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

        verifySuspend { sdkEventDistributor.registerEvent(expectedEvent) }
    }

    @Test
    fun testChangeApplicationCode_shouldReturn_whenAppCode_isEmpty() = runTest {
        everySuspend { mockLogger.error(message = any()) } returns Unit
        shouldThrow<SdkException.InvalidApplicationCodeException> {
            configInternal.changeApplicationCode(" ")
        }

        verifySuspend(mode = VerifyMode.exactly(0)) { sdkEventDistributor.registerEvent(any()) }
    }

    @Test
    fun testChangeApplicationCode_shouldThrow_whenAppCode_isInvalid() = runTest {
        forAll(
            row("EMS 11-C3FD3"),
            row("EMS"),
            row("-EMS11-C3FD3"),
            row("EMS11--C3FD3"),
            row("EMS11-C3FD3-")
        ) { appCode ->
            everySuspend { mockLogger.error(any(), any<Throwable>(), true) } returns Unit
            shouldThrow<SdkException.InvalidApplicationCodeException> {
                configInternal.changeApplicationCode(appCode)
            }
        }
        verifySuspend(mode = VerifyMode.exactly(0)) { sdkEventDistributor.registerEvent(any()) }
    }

    @Test
    fun testChangeMerchantId_shouldEmitChangeMerchantIdEvent_toSdkEventFlow() = runTest {
        val expectedEvent = SdkEvent.Internal.Sdk.ChangeMerchantId(
            id = UUID,
            merchantId = MERCHANT_ID,
            timestamp = TIMESTAMP
        )

        configInternal.changeMerchantId(MERCHANT_ID)

        verifySuspend { sdkEventDistributor.registerEvent(expectedEvent) }
    }

    @Test
    fun testSetLanguage_shouldCallHandleLanguage_onLanguageHandler() = runTest {
        val language = "hu-HU"

        everySuspend { mockLanguageHandler.handleLanguage(language) } returns Unit

        configInternal.setLanguage(language)

        verifySuspend {
            mockLanguageHandler.handleLanguage(language)
        }
    }

    @Test
    fun testResetLanguage_shouldCallHandleLanguage_withNull_inLanguageHandler() = runTest {
        everySuspend { mockLanguageHandler.handleLanguage(null) } returns Unit

        configInternal.resetLanguage()

        verifySuspend {
            mockLanguageHandler.handleLanguage(null)
        }
    }

}