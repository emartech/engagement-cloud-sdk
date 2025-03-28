package com.emarsys.integration

import com.emarsys.AndroidEmarsysConfig
import com.emarsys.Emarsys
import com.emarsys.context.SdkContextApi
import com.emarsys.core.session.SessionContext
import com.emarsys.core.storage.StorageConstants
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.di.SdkKoinIsolationContext.koin
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.koin.core.Koin
import org.koin.core.component.get
import org.koin.test.KoinTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ContactClientIntegrationTests : KoinTest {
    override fun getKoin(): Koin = koin

    private lateinit var sessionContext: SessionContext
    private lateinit var sdkContext: SdkContextApi
    private lateinit var stringStorage: StringStorageApi

    @BeforeTest
    fun setup() = runTest {
        Emarsys.initialize()

        sessionContext = get<SessionContext>()
        sdkContext = get<SdkContextApi>()
        stringStorage = get<StringStorageApi>()
    }

    @AfterTest
    fun tearDown() = runTest {
        sdkContext.config = null
        stringStorage.put(StorageConstants.SDK_CONFIG_KEY, null)
    }

    @Test
    fun testLinkContact() = runTest {
        Emarsys.enableTracking(AndroidEmarsysConfig("EMS11-C3FD3"))
        sessionContext.contactToken = null
        sessionContext.refreshToken = null

        Emarsys.linkContact(2575, "test2@test.com")

        backgroundScope.launch {
            sessionContext.contactToken shouldNotBe null
            sessionContext.refreshToken shouldNotBe null
        }
    }

    @Test
    fun testLinkContact_predictOnly() = runTest {
        sessionContext.contactToken = null
        sessionContext.refreshToken = null
        sessionContext.clientState = null
        sessionContext.deviceEventState = null
        sessionContext.sessionId = null
        sessionContext.clientId = null
        sessionContext.contactFieldValue = null
        sessionContext.openIdToken = null
        sdkContext.config = null

        Emarsys.enableTracking(AndroidEmarsysConfig(merchantId = "1DF86BF95CBE8F19"))

        Emarsys.linkContact(2575, "test2@test.com")

        backgroundScope.launch {
            sessionContext.contactToken shouldNotBe null
            sessionContext.refreshToken shouldNotBe null
        }
    }

    @Test
    fun testUnlinkContact() = runTest {
        Emarsys.enableTracking(AndroidEmarsysConfig("EMS11-C3FD3"))
        Emarsys.linkContact(2575, "test2@test.com")

        val contactToken = sessionContext.contactToken

        Emarsys.unlinkContact()

        backgroundScope.launch {
            sessionContext.contactToken shouldNotBe contactToken
        }
    }

}
