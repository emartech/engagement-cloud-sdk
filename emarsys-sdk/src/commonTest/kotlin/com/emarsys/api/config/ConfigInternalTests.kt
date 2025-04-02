package com.emarsys.api.config

import com.emarsys.core.channel.SdkEventDistributorApi
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
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.test.BeforeTest
import kotlin.test.Test

class ConfigInternalTests {
    private companion object {
        const val UUID = "testUUID"
        val TIMESTAMP = Clock.System.now()
        const val APPCODE = "testApplicationCode"
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
        everySuspend { mockLogger.debug(tag = any()) } returns Unit
        mockUuidProvider = mock()
        every { mockUuidProvider.provide() } returns UUID
        mockTimestampProvider = mock()
        every { mockTimestampProvider.provide() } returns TIMESTAMP
        mockLanguageHandler = mock()
        sdkEventDistributor = mock(MockMode.autofill)
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
        val expectedEvent = SdkEvent.Internal.Sdk.ChangeAppCode(UUID, buildJsonObject {
            put("applicationCode", JsonPrimitive(APPCODE))
        }, TIMESTAMP)

        configInternal.changeApplicationCode(APPCODE)

        verifySuspend { sdkEventDistributor.registerAndStoreEvent(expectedEvent) }
    }

    @Test
    fun testChangeMerchantId_shouldEmitChangeMerchantIdEvent_toSdkEventFlow() = runTest {
        val expectedEvent = SdkEvent.Internal.Sdk.ChangeMerchantId(UUID, buildJsonObject {
            put("merchantId", JsonPrimitive(MERCHANT_ID))
        }, TIMESTAMP)

        configInternal.changeMerchantId(MERCHANT_ID)

        verifySuspend { sdkEventDistributor.registerAndStoreEvent(expectedEvent) }
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