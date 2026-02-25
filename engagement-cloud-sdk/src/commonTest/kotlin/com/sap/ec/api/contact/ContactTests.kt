package com.sap.ec.api.contact

import com.sap.ec.api.SdkState
import com.sap.ec.context.DefaultUrls
import com.sap.ec.context.SdkContext
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.log.LogLevel
import com.sap.ec.core.storage.StringStorageApi
import com.sap.ec.di.SdkKoinIsolationContext.koin
import com.sap.ec.fake.FakeStringStorage
import com.sap.ec.util.JsonUtil
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
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

@OptIn(ExperimentalCoroutinesApi::class)
class ContactTests : KoinTest {

    override fun getKoin(): Koin = koin

    private companion object {
        const val CONTACT_FIELD_VALUE = "testContactFieldValue"
        const val OPEN_ID_TOKEN = "testOpenIdToken"
        val testException = Exception()
    }

    private lateinit var testModule: Module

    private lateinit var sdkContext: SdkContextApi
    private lateinit var mockLoggingContact: ContactInstance
    private lateinit var mockGathererContact: ContactInstance
    private lateinit var mockContactInternal: ContactInstance
    private lateinit var contact: Contact<ContactInstance, ContactInstance, ContactInstance>

    private val mainDispatcher = StandardTestDispatcher()

    init {
        Dispatchers.setMain(mainDispatcher)
    }

    @BeforeTest
    fun setup() = runTest {
        testModule = module {
            single<StringStorageApi> { FakeStringStorage() }
            single<Json> { JsonUtil.json }
        }
        koin.loadModules(listOf(testModule))

        mockLoggingContact = mock()
        mockGathererContact = mock()
        mockContactInternal = mock()
        sdkContext = SdkContext(
            sdkDispatcher = StandardTestDispatcher(),
            mainDispatcher = mainDispatcher,
            defaultUrls = DefaultUrls("", "", "", "", "", "", "", ""),
            remoteLogLevel = LogLevel.Error,
            features = mutableSetOf(),
            logBreadcrumbsQueueSize = 10,
            onContactLinkingFailed = null
        )

        everySuspend { mockLoggingContact.activate() } returns Unit
        everySuspend { mockGathererContact.activate() } returns Unit
        everySuspend { mockContactInternal.activate() } returns Unit
        contact =
            Contact(mockLoggingContact, mockGathererContact, mockContactInternal, sdkContext)
        contact.registerOnContext()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        koin.unloadModules(listOf(testModule))
    }

    @Test
    fun testLink_unInitializedState() = runTest {
        everySuspend {
            mockLoggingContact.link(
                CONTACT_FIELD_VALUE
            )
        } returns Unit

        sdkContext.setSdkState(SdkState.UnInitialized)
        contact.link(CONTACT_FIELD_VALUE)

        verifySuspend {
            mockLoggingContact.link(
                CONTACT_FIELD_VALUE
            )
        }
    }

    @Test
    fun testLink_onHoldState() = runTest {
        everySuspend {
            mockGathererContact.link(
                CONTACT_FIELD_VALUE
            )
        } returns Unit

        sdkContext.setSdkState(SdkState.OnHold)
        contact.link(CONTACT_FIELD_VALUE)

        verifySuspend {
            mockGathererContact.link(
                CONTACT_FIELD_VALUE
            )
        }
    }

    @Test
    fun testLink_activeState() = runTest {
        everySuspend {
            mockContactInternal.link(
                CONTACT_FIELD_VALUE
            )
        } returns Unit

        sdkContext.setSdkState(SdkState.Active)
        contact.link(CONTACT_FIELD_VALUE)

        verifySuspend {
            mockContactInternal.link(

                CONTACT_FIELD_VALUE
            )
        }
    }

    @Test
    fun testLink_activeState_throws() = runTest {
        everySuspend {
            mockContactInternal.link(
                CONTACT_FIELD_VALUE
            )
        } throws testException

        sdkContext.setSdkState(SdkState.Active)
        val result = contact.link(CONTACT_FIELD_VALUE)

        result.exceptionOrNull() shouldBe testException
    }

    @Test
    fun testLinkAuthenticated_unInitializedState() = runTest {
        everySuspend {
            mockLoggingContact.linkAuthenticated(
                OPEN_ID_TOKEN
            )
        } returns Unit

        sdkContext.setSdkState(SdkState.UnInitialized)
        contact.linkAuthenticated(OPEN_ID_TOKEN)

        verifySuspend {
            mockLoggingContact.linkAuthenticated(

                OPEN_ID_TOKEN
            )
        }
    }

    @Test
    fun testLinkAuthenticated_onHoldState() = runTest {
        everySuspend {
            mockGathererContact.linkAuthenticated(

                OPEN_ID_TOKEN
            )
        } returns Unit

        sdkContext.setSdkState(SdkState.OnHold)
        contact.linkAuthenticated(OPEN_ID_TOKEN)

        verifySuspend {
            mockGathererContact.linkAuthenticated(

                OPEN_ID_TOKEN
            )
        }
    }

    @Test
    fun testLinkAuthenticated_activeState() = runTest {
        everySuspend {
            mockContactInternal.linkAuthenticated(
                OPEN_ID_TOKEN
            )
        } returns Unit

        sdkContext.setSdkState(SdkState.Active)
        contact.linkAuthenticated(OPEN_ID_TOKEN)

        verifySuspend {
            mockContactInternal.linkAuthenticated(
                OPEN_ID_TOKEN
            )
        }
    }

    @Test
    fun testLinkAuthenticated_activeState_throws() = runTest {
        everySuspend {
            mockContactInternal.linkAuthenticated(
                OPEN_ID_TOKEN
            )
        } throws testException

        sdkContext.setSdkState(SdkState.Active)
        val result = contact.linkAuthenticated(OPEN_ID_TOKEN)

        result.exceptionOrNull() shouldBe testException
    }

    @Test
    fun testUnlink_UnInitializedState() = runTest {
        everySuspend {
            mockLoggingContact.unlink()
        } returns Unit

        sdkContext.setSdkState(SdkState.UnInitialized)
        contact.unlink()

        verifySuspend {
            mockLoggingContact.unlink()
        }
    }

    @Test
    fun testUnlink_onHoldState() = runTest {
        everySuspend {
            mockGathererContact.unlink()
        } returns Unit

        sdkContext.setSdkState(SdkState.OnHold)
        contact.unlink()

        verifySuspend {
            mockGathererContact.unlink()
        }
    }

    @Test
    fun test_unlink_activeState() = runTest {
        everySuspend {
            mockContactInternal.unlink()
        } returns Unit

        sdkContext.setSdkState(SdkState.Active)
        contact.unlink()

        verifySuspend {
            mockContactInternal.unlink()
        }
    }

    @Test
    fun test_unlink_activeState_throws() = runTest {
        everySuspend {
            mockContactInternal.unlink()
        } throws testException

        sdkContext.setSdkState(SdkState.Active)
        val result = contact.unlink()

        result.exceptionOrNull() shouldBe testException
    }

}