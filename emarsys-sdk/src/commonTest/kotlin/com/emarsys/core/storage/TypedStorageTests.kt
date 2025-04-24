package com.emarsys.core.storage

import com.emarsys.EmarsysConfig
import com.emarsys.core.log.Logger
import com.emarsys.util.JsonUtil
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class TypedStorageTests {
    private companion object {
        const val APP_CODE = "testApplicationCode"
        const val MERCHANT_ID = "testMerchantId"
        const val SHARED_SECRET = "testSharedSecret"
        val CONFIG = EmarsysConfig(APP_CODE, MERCHANT_ID, SHARED_SECRET)
        val CONFIG_STRING = JsonUtil.json.encodeToString(CONFIG)
    }

    private lateinit var mockStringStorage: StringStorageApi
    private lateinit var mockLogger: Logger
    private lateinit var typedStorage: TypedStorage

    @BeforeTest
    fun setUp() {
        mockStringStorage = mock()
        mockLogger = mock()
        everySuspend { mockLogger.error(message = any(), throwable = any()) } returns Unit
        typedStorage = TypedStorage(mockStringStorage, JsonUtil.json, mockLogger)
    }

    @Test
    fun testTypedStorage() = runTest {
        val key = "testKey"
        every { mockStringStorage.put(any(), any()) } returns Unit
        every { mockStringStorage.get(key) } returns CONFIG_STRING
        val expected = EmarsysConfig(APP_CODE, MERCHANT_ID, SHARED_SECRET)

        typedStorage.put(key, EmarsysConfig.serializer(), expected)

        val result: EmarsysConfig? = typedStorage.get(key, EmarsysConfig.serializer())

        result shouldBe expected
    }

    @Test
    fun testGet_shouldNotCrashOnErrorAndLog() = runTest {
        val key = "testKey"
        every { mockStringStorage.get(key) } returns """{"notValidKey": "testApplicationCode", "applicationCode": {1234}}"""

        typedStorage.get(key, EmarsysConfig.serializer())

        verifySuspend { mockLogger.error("get", throwable = any()) }
    }

    @Test
    fun testRemove_shouldDelegateToStringStorage() = runTest {
        val key = "testKey"
        everySuspend { mockStringStorage.put(key, null) } returns Unit

        typedStorage.remove(key)

        verifySuspend { mockStringStorage.put(key, null) }
    }
}