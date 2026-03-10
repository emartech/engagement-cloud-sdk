package com.sap.ec.context

import com.sap.ec.TestEngagementCloudSDKConfig
import com.sap.ec.config.SdkConfig
import com.sap.ec.enable.config.SdkConfigStoreApi
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SdkContextTests {
    private companion object {
        val TEST_CONFIG = TestEngagementCloudSDKConfig("testAppCode")
    }

    private var features: MutableSet<Features> = mutableSetOf()
    private lateinit var sdkDispatcher: CoroutineDispatcher
    private lateinit var mainDispatcher: CoroutineDispatcher
    private lateinit var mockDefaultUrls: DefaultUrlsApi
    private lateinit var mockSdkConfigStore: SdkConfigStoreApi<SdkConfig>
    private lateinit var sdkContext: SdkContextApi


    @BeforeTest
    fun setup() {
        sdkDispatcher = StandardTestDispatcher()
        mainDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(mainDispatcher)
        mockDefaultUrls = mock(MockMode.autofill)
        mockSdkConfigStore = mock(MockMode.autofill)

        sdkContext = SdkContext(
            sdkDispatcher = sdkDispatcher,
            mainDispatcher = mainDispatcher,
            onContactLinkingFailed = null,
            defaultUrls = mockDefaultUrls,
            features = features,
            sdkConfigStore = mockSdkConfigStore
        )
    }

    @Test
    fun testGetSdkConfig_shouldReturnConfigFromStore() = runTest {
        everySuspend { mockSdkConfigStore.load() } returns TEST_CONFIG

        sdkContext.getSdkConfig() shouldBe TEST_CONFIG
        verifySuspend { mockSdkConfigStore.load() }
    }

    @Test
    fun testGetSdkConfig_shouldReturnCachedConfigOnSecondCall() = runTest {
        everySuspend { mockSdkConfigStore.load() } returns TEST_CONFIG

        sdkContext.getSdkConfig() shouldBe TEST_CONFIG
        sdkContext.getSdkConfig() shouldBe TEST_CONFIG
        verifySuspend(VerifyMode.exactly(1)) { mockSdkConfigStore.load() }
    }

    @Test
    fun testGetSdkConfig_shouldReturnNullWhenStoreReturnsNull() = runTest {
        everySuspend { mockSdkConfigStore.load() } returns null

        sdkContext.getSdkConfig() shouldBe null
        verifySuspend { mockSdkConfigStore.load() }
    }

    @Test
    fun testSetSdkConfig_shouldStoreConfig() = runTest {
        sdkContext.setSdkConfig(TEST_CONFIG)

        verifySuspend { mockSdkConfigStore.store(TEST_CONFIG) }
        sdkContext.getSdkConfig() shouldBe TEST_CONFIG
    }

    @Test
    fun testSetSdkConfig_shouldClearStoreWhenConfigToSetIsNull() = runTest {
        sdkContext.setSdkConfig(null)

        verifySuspend { mockSdkConfigStore.clear() }
    }

    @Test
    fun testSetSdkConfig_shouldReturnCachedConfigWithoutCallingStore() = runTest {
        sdkContext.setSdkConfig(TEST_CONFIG)
        val result = sdkContext.getSdkConfig()

        result shouldBe TEST_CONFIG
        verifySuspend { mockSdkConfigStore.store(TEST_CONFIG) }
        verifySuspend(VerifyMode.exactly(0)) { mockSdkConfigStore.load() }
    }
}