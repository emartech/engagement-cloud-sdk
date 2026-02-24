package com.sap.ec.core.language

import com.sap.ec.SdkConstants.LANGUAGE_STORAGE_KEY
import com.sap.ec.context.Features
import com.sap.ec.context.Features.EmbeddedMessaging
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.channel.SdkEventWaiterApi
import com.sap.ec.core.exceptions.SdkException.PreconditionFailedException
import com.sap.ec.core.log.Logger
import com.sap.ec.core.storage.StringStorageApi
import com.sap.ec.event.SdkEvent
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture.Companion.slot
import dev.mokkery.matcher.capture.capture
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class LanguageHandlerTests {

    private lateinit var mockLanguageTagValidator: LanguageTagValidatorApi
    private lateinit var mockStringStorage: StringStorageApi
    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi
    private lateinit var mockLogger: Logger
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var mockEventWaiter: SdkEventWaiterApi

    private lateinit var languageHandler: LanguageHandlerApi

    @BeforeTest
    fun setUp() = runTest {
        mockLanguageTagValidator = mock()
        mockStringStorage = mock()
        mockSdkEventDistributor = mock()
        mockLogger = mock()
        everySuspend {
            mockLogger.debug(any<String>())
        } returns Unit
        mockSdkContext = mock()
        mockEventWaiter = mock()

        languageHandler = LanguageHandler(
            mockLanguageTagValidator,
            mockStringStorage,
            mockSdkEventDistributor,
            mockLogger,
            mockSdkContext
        )
    }

    @Test
    fun testHandleLanguage_should_throw_exception_when_language_is_not_supported() = runTest {
        everySuspend { mockLanguageTagValidator.isValid("not-real-language-code") } returns false
        everySuspend { mockLogger.error(any<String>()) } returns Unit

        shouldThrow<PreconditionFailedException> {
            languageHandler.handleLanguage("not-real-language-code")
        }
    }

    @Test
    fun testHandleLanguage_shouldClearStorage_andEmitRegisterDeviceInfoEvent_whenLanguageIsNull_andRegisterFetchMetaEvent_onSuccessResult() =
        runTest {
            val successDeviceEventResponse = SdkEvent.Internal.Sdk.Answer.Response(
                originId = "any",
                Result.success(Unit)
            )
            everySuspend { mockEventWaiter.await<Unit>() } returns successDeviceEventResponse
            everySuspend { mockSdkEventDistributor.registerEvent(any<SdkEvent.Internal.Sdk.RegisterDeviceInfo>()) } returns mockEventWaiter
            val fetchMetaSlot = slot<SdkEvent.Internal.EmbeddedMessaging.FetchMeta>()
            everySuspend { mockSdkEventDistributor.registerEvent(capture(fetchMetaSlot)) } returns mockEventWaiter
            everySuspend { mockStringStorage.put(any(), any()) } returns Unit
            everySuspend { mockSdkContext.features } returns mutableSetOf(EmbeddedMessaging)

            languageHandler.handleLanguage(null)

            verifySuspend {
                mockStringStorage.put(LANGUAGE_STORAGE_KEY, null)
                mockSdkEventDistributor.registerEvent(any<SdkEvent.Internal.Sdk.RegisterDeviceInfo>())
                mockSdkEventDistributor.registerEvent(any<SdkEvent.Internal.EmbeddedMessaging.FetchMeta>())
            }
            fetchMetaSlot.values.first().type shouldBe "fetchMeta"
        }

    @Test
    fun testHandleLanguage_shouldClearStorage_andEmitRegisterDeviceInfoEvent_whenLanguageIsNull_andNotRegisterFetchMetaEvent_onFailureResult() =
        runTest {
            val failureDeviceEventResponse = SdkEvent.Internal.Sdk.Answer.Response<Exception>(
                originId = "any",
                Result.failure(RuntimeException("any"))
            )
            everySuspend { mockEventWaiter.await<Exception>() } returns failureDeviceEventResponse
            everySuspend { mockSdkEventDistributor.registerEvent(any()) } returns mockEventWaiter
            everySuspend { mockStringStorage.put(any(), any()) } returns Unit
            everySuspend { mockSdkContext.features } returns mutableSetOf(EmbeddedMessaging)
            everySuspend { mockLogger.error(any(), any<Throwable>()) } returns Unit

            languageHandler.handleLanguage(null)

            verifySuspend {
                mockStringStorage.put(LANGUAGE_STORAGE_KEY, null)
                mockSdkEventDistributor.registerEvent(any<SdkEvent.Internal.Sdk.RegisterDeviceInfo>())
            }
            verifySuspend(VerifyMode.exactly(0)) { mockSdkEventDistributor.registerEvent(any<SdkEvent.Internal.EmbeddedMessaging.FetchMeta>()) }
        }

    @Test
    fun testHandleLanguage_shouldUpdateStorage_andEmitRegisterDeviceInfoEvent_whenLanguageIsNotNull_andRegisterFetchMetaEvent_onSuccessResult() =
        runTest {
            val successDeviceEventResponse = SdkEvent.Internal.Sdk.Answer.Response(
                originId = "any",
                Result.success(Unit)
            )
            everySuspend { mockEventWaiter.await<Unit>() } returns successDeviceEventResponse
            everySuspend { mockSdkEventDistributor.registerEvent(any<SdkEvent.Internal.Sdk.RegisterDeviceInfo>()) } returns mockEventWaiter
            val fetchMetaSlot = slot<SdkEvent.Internal.EmbeddedMessaging.FetchMeta>()
            everySuspend { mockSdkEventDistributor.registerEvent(capture(fetchMetaSlot)) } returns mockEventWaiter
            everySuspend { mockStringStorage.put(any(), any()) } returns Unit
            everySuspend { mockSdkContext.features } returns mutableSetOf(EmbeddedMessaging)
            everySuspend { mockLanguageTagValidator.isValid("zh-Hans-CN") } returns true

            languageHandler.handleLanguage("zh-Hans-CN")

            verifySuspend {
                mockStringStorage.put(LANGUAGE_STORAGE_KEY, "zh-Hans-CN")
                mockSdkEventDistributor.registerEvent(any<SdkEvent.Internal.Sdk.RegisterDeviceInfo>())
                mockSdkEventDistributor.registerEvent(any<SdkEvent.Internal.EmbeddedMessaging.FetchMeta>())
            }
            fetchMetaSlot.values.first().type shouldBe "fetchMeta"
        }

    @Test
    fun testHandleLanguage_shouldUpdateStorage_andEmitRegisterDeviceInfoEvent_whenLanguageIsNotNull_andNotRegisterFetchMetaEvent_onFailureResult() =
        runTest {
            val failureDeviceEventResponse = SdkEvent.Internal.Sdk.Answer.Response<Exception>(
                originId = "any",
                Result.failure(RuntimeException("any"))
            )
            everySuspend { mockEventWaiter.await<Exception>() } returns failureDeviceEventResponse
            everySuspend { mockSdkEventDistributor.registerEvent(any()) } returns mockEventWaiter
            everySuspend { mockStringStorage.put(any(), any()) } returns Unit
            everySuspend { mockSdkContext.features } returns mutableSetOf(EmbeddedMessaging)
            everySuspend { mockLanguageTagValidator.isValid("zh-Hans-CN") } returns true
            everySuspend { mockLogger.error(any(), any<Throwable>()) } returns Unit

            languageHandler.handleLanguage("zh-Hans-CN")

            verifySuspend {
                mockStringStorage.put(LANGUAGE_STORAGE_KEY, "zh-Hans-CN")
                mockSdkEventDistributor.registerEvent(any<SdkEvent.Internal.Sdk.RegisterDeviceInfo>())
            }
            verifySuspend(VerifyMode.exactly(0)) { mockSdkEventDistributor.registerEvent(any<SdkEvent.Internal.EmbeddedMessaging.FetchMeta>()) }
        }

    @Test
    fun testHandleLanguage_shouldUpdateStorage_andEmitRegisterDeviceInfoEvent_whenLanguageIsNotNull_andNotRegisterFetchMetaEvent_onSuccessResult_whenSdkContextDoesntContainEMFeature() =
        runTest {
            val successDeviceEventResponse = SdkEvent.Internal.Sdk.Answer.Response(
                originId = "any",
                Result.success(Unit)
            )
            everySuspend { mockEventWaiter.await<Unit>() } returns successDeviceEventResponse
            everySuspend { mockSdkEventDistributor.registerEvent(any()) } returns mockEventWaiter
            everySuspend { mockStringStorage.put(any(), any()) } returns Unit
            everySuspend { mockSdkContext.features } returns mutableSetOf()
            everySuspend { mockLanguageTagValidator.isValid("zh-Hans-CN") } returns true
            everySuspend { mockLogger.debug(any<String>()) } returns Unit

            languageHandler.handleLanguage("zh-Hans-CN")

            verifySuspend {
                mockStringStorage.put(LANGUAGE_STORAGE_KEY, "zh-Hans-CN")
                mockSdkEventDistributor.registerEvent(any<SdkEvent.Internal.Sdk.RegisterDeviceInfo>())
            }
            verifySuspend(VerifyMode.exactly(0)) { mockSdkEventDistributor.registerEvent(any<SdkEvent.Internal.EmbeddedMessaging.FetchMeta>()) }
        }

    @Test
    fun testHandleLanguage_shouldClearStorage_andEmitRegisterDeviceInfoEvent_whenLanguageIsNull_andNotRegisterFetchMetaEvent_onSuccessResult_whenSdkContextDoesntContainEMFeature() =
        runTest {
            val successDeviceEventResponse = SdkEvent.Internal.Sdk.Answer.Response(
                originId = "any",
                Result.success(Unit)
            )
            everySuspend { mockEventWaiter.await<Unit>() } returns successDeviceEventResponse
            everySuspend { mockSdkEventDistributor.registerEvent(any()) } returns mockEventWaiter
            everySuspend { mockStringStorage.put(any(), any()) } returns Unit
            everySuspend { mockSdkContext.features } returns mutableSetOf()
            everySuspend { mockLogger.debug(any<String>()) } returns Unit

            languageHandler.handleLanguage(null)

            verifySuspend {
                mockStringStorage.put(LANGUAGE_STORAGE_KEY, null)
                mockSdkEventDistributor.registerEvent(any<SdkEvent.Internal.Sdk.RegisterDeviceInfo>())
            }
            verifySuspend(VerifyMode.exactly(0)) { mockSdkEventDistributor.registerEvent(any<SdkEvent.Internal.EmbeddedMessaging.FetchMeta>()) }
        }
}