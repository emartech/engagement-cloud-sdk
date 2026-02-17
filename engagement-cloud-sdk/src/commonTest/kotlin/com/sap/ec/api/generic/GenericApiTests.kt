package com.sap.ec.api.generic

import com.sap.ec.api.SdkState.Active
import com.sap.ec.api.SdkState.Initialized
import com.sap.ec.api.SdkState.OnHold
import com.sap.ec.api.SdkState.UnInitialized
import com.sap.ec.api.contact.ContactContext
import com.sap.ec.api.contact.ContactContextApi
import com.sap.ec.api.contact.ContactGatherer
import com.sap.ec.api.contact.ContactInternal
import com.sap.ec.api.contact.LoggingContact
import com.sap.ec.context.DefaultUrls
import com.sap.ec.context.SdkContext
import com.sap.ec.core.log.LogLevel
import com.sap.ec.core.log.Logger
import com.sap.ec.core.storage.StringStorageApi
import com.sap.ec.di.SdkKoinIsolationContext.koin
import com.sap.ec.fake.FakeStringStorage
import com.sap.ec.util.JsonUtil
import dev.mokkery.MockMode
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import org.koin.core.Koin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.test.KoinTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class GenericApiTests : KoinTest {

    override fun getKoin(): Koin = koin

    private lateinit var testModule: Module

    private lateinit var mockSdkLogger: Logger
    private lateinit var loggingContact: LoggingContact
    private lateinit var contactGatherer: ContactGatherer
    private lateinit var contactInternal: ContactInternal
    private lateinit var sdkContext: SdkContext
    private lateinit var contactContext: ContactContextApi
    private lateinit var genericApi: GenericApi<LoggingContact, ContactGatherer, ContactInternal>

    @BeforeTest
    fun setup() = runTest {
        testModule = module {
            single<StringStorageApi> { FakeStringStorage() }
            single<Json> { JsonUtil.json }
        }
        koin.loadModules(listOf(testModule))

        val mainDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(mainDispatcher)
        contactContext = ContactContext(mutableListOf())
        mockSdkLogger = mock(MockMode.autofill)
        loggingContact = LoggingContact(mockSdkLogger)
        contactGatherer = ContactGatherer(contactContext, mockSdkLogger)
        contactInternal =
            ContactInternal(contactContext, mockSdkLogger, sdkEventDistributor = mock())
        sdkContext = SdkContext(
            StandardTestDispatcher(),
            mainDispatcher,
            DefaultUrls("", "", "", "", "", "", ""),
            LogLevel.Error,
            mutableSetOf(),
            logBreadcrumbsQueueSize = 10
        )
        genericApi = GenericApi(loggingContact, contactGatherer, contactInternal, sdkContext)
        genericApi.registerOnContext()
    }

    @AfterTest
    fun tearDown() {
        koin.unloadModules(listOf(testModule))
    }

    @Test
    fun testActive_whenSdkState_isUnInitialized() = runTest {
        sdkContext.setSdkState(UnInitialized)

        advanceUntilIdle()

        genericApi.activeInstance shouldBe loggingContact
    }

    @Test
    fun testActive_whenSdkState_isOnHold() = runTest {
        sdkContext.setSdkState(OnHold)

        advanceUntilIdle()

        genericApi.activeInstance shouldBe contactGatherer
    }

    @Test
    fun testActive_whenSdkState_isActive() = runTest {
        sdkContext.setSdkState(Active)

        advanceUntilIdle()

        genericApi.activeInstance shouldBe contactInternal
    }

    @Test
    fun testActive_whenSdkState_isInitialized() = runTest {
        sdkContext.setSdkState(Initialized)

        advanceUntilIdle()

        genericApi.activeInstance shouldBe loggingContact
    }
}