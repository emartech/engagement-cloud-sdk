package com.emarsys.core.providers

import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.device.DeviceInfoCollectorApi
import com.emarsys.core.networking.UserAgentProvider
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.resetAnswers
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class UserAgentProviderTests {
    private companion object {
        val DEVICE_INFO = DeviceInfo(
            "testPlatform",
            "testAppVersion",
            "testDeviceModel",
            "testOsVersion",
            "testSdkVersion",
            "testLanguageCode",
            "testTimeZone",
            "testClientId"
        )
    }

    private lateinit var mockDeviceInfoCollector: DeviceInfoCollectorApi
    private lateinit var userAgentProvider: UserAgentProvider

    @BeforeTest
    fun setUp() {
        mockDeviceInfoCollector = mock()
        every { mockDeviceInfoCollector.collect() } returns Json.encodeToString(DEVICE_INFO)

        userAgentProvider = UserAgentProvider(mockDeviceInfoCollector, Json)
    }

    @AfterTest
    fun tearDown() {
        resetAnswers(mockDeviceInfoCollector)
    }

    @Test
    fun testHeaderName() = runTest {
        UserAgentProvider.USER_AGENT_HEADER_NAME shouldBe "User-Agent"
    }

    @Test
    fun testProvide() = runTest {
        userAgentProvider.provide() shouldBe "Emarsys SDK testSdkVersion testPlatform testOsVersion"
    }
}
