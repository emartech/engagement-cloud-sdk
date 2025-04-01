package com.emarsys.integration

import com.emarsys.Emarsys
import com.emarsys.EmarsysConfig
import com.emarsys.context.SdkContext
import com.emarsys.context.SdkContextApi
import com.emarsys.core.session.SessionContext
import com.emarsys.core.storage.StorageConstants
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.di.SdkKoinIsolationContext.koin
import com.emarsys.networking.clients.device.DeviceClientApi
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.test.runTest
import org.koin.core.Koin
import org.koin.core.component.get
import org.koin.test.KoinTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test

class DeviceClientIntegrationTests: KoinTest {
    override fun getKoin(): Koin = koin

    private lateinit var sessionContext: SessionContext
    private lateinit var deviceClient: DeviceClientApi
    private lateinit var sdkContext: SdkContextApi
    private lateinit var stringStorage: StringStorageApi

    @BeforeTest
    fun setup() = runTest {
        Emarsys.initialize()

        sessionContext = get<SessionContext>()
        sdkContext = get<SdkContext>()
        deviceClient = get<DeviceClientApi>()
        stringStorage = get<StringStorageApi>()
    }

    @AfterTest
    fun tearDown() = runTest {
        sdkContext.config = null
        stringStorage.put(StorageConstants.SDK_CONFIG_KEY, null)
    }

    @Ignore //TODO: V4 client endpoint does not return tokens at the moment(31.10.2024); test should pass after BE is fixed
    @Test
    fun testRegisterDeviceInfo_whenAnonymousContactIsTurnedOn() = runTest {

        Emarsys.initialize()
        Emarsys.enableTracking(EmarsysConfig("EMS11-C3FD3"))

        sessionContext.contactToken = null
        sessionContext.refreshToken = null
        sessionContext.clientState = null

        deviceClient.registerDeviceInfo()

        sessionContext.contactToken shouldNotBe null
        sessionContext.refreshToken shouldNotBe null
        sessionContext.clientState shouldNotBe null
    }

    @Ignore //TODO: this is a real test case when the backend get rid off their inner flipper "SET_ANON_CONTACT_ON_CLIENT_REGISTRATION"
    @Test
    fun testRegisterDeviceInfo_whenAnonymousContactIsTurnedOff() = runTest {

        Emarsys.initialize()
        Emarsys.enableTracking(EmarsysConfig("EMSD1-A342A"))

        sessionContext.contactToken = null
        sessionContext.refreshToken = null
        sessionContext.clientState = null

        deviceClient.registerDeviceInfo()

        sessionContext.contactToken shouldBe null
        sessionContext.refreshToken shouldBe null
        sessionContext.clientState shouldBe null
    }

}
