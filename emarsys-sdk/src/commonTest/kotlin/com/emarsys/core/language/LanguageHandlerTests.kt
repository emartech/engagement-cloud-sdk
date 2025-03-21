package com.emarsys.core.language

import com.emarsys.SdkConstants.LANGUAGE_STORAGE_KEY
import com.emarsys.core.exceptions.PreconditionFailedException
import com.emarsys.core.log.Logger
import com.emarsys.core.providers.Provider
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.networking.clients.event.model.SdkEvent
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.assertions.throwables.shouldThrow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class LanguageHandlerTests {

    private lateinit var mockSupportedLanguagesProvider: Provider<List<String>>
    private lateinit var mockStringStorage: StringStorageApi
    private lateinit var mockSdkEvents: MutableSharedFlow<SdkEvent>
    private lateinit var mockLogger: Logger

    private lateinit var languageHandler: LanguageHandlerApi

    @BeforeTest
    fun setUp() = runTest {
        mockSupportedLanguagesProvider = mock()
        every {
            mockSupportedLanguagesProvider.provide()
        } returns listOf("zh-Hant-HK", "fr-LU", "en-HU")
        mockStringStorage = mock()
        mockSdkEvents = mock()
        mockLogger = mock()
        everySuspend {
            mockLogger.debug(any(), any<String>())
        } returns Unit

        languageHandler = LanguageHandler(
            mockSupportedLanguagesProvider,
            mockStringStorage,
            mockSdkEvents,
            mockLogger
        )
    }

    @Test
    fun testHandleLanguage_should_throw_exception_when_language_is_not_supported() = runTest {
        shouldThrow<PreconditionFailedException> {
            languageHandler.handleLanguage("not-real-language-code")
        }
    }

    @Test
    fun testHandleLanguage_should_clearStorage_and_emit_deviceInfoUpdateRequired_event_when_language_is_null() = runTest {
        everySuspend { mockStringStorage.put(any(), any()) } returns Unit
        everySuspend { mockSdkEvents.emit(any()) } returns Unit

        languageHandler.handleLanguage(null)

        verifySuspend {
            mockStringStorage.put(LANGUAGE_STORAGE_KEY, null)
            mockSdkEvents.emit(any<SdkEvent.Internal.Sdk.DeviceInfoUpdateRequired>())
        }
    }

    @Test
    fun testHandleLanguage_should_store_language_and_emit_deviceInfoUpdateRequired_event_when_language_is_valid() = runTest {
        everySuspend { mockStringStorage.put(any(), any()) } returns Unit
        everySuspend { mockSdkEvents.emit(any()) } returns Unit

        languageHandler.handleLanguage("zh-Hant-HK")

        verifySuspend {
            mockStringStorage.put(LANGUAGE_STORAGE_KEY, "zh-Hant-HK")
            mockSdkEvents.emit(any<SdkEvent.Internal.Sdk.DeviceInfoUpdateRequired>())
        }
    }

}