package com.emarsys.integration

import com.emarsys.AndroidEmarsysConfig
import com.emarsys.Emarsys
import com.emarsys.api.push.PushConstants
import com.emarsys.api.push.PushConstants.PUSH_TOKEN_STORAGE_KEY
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.di.SdkKoinIsolationContext.koin
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.test.runTest
import org.koin.core.Koin
import org.koin.core.component.get
import org.koin.test.KoinTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class PushClientIntegrationTests : KoinTest {
    override fun getKoin(): Koin = koin

    private lateinit var stringStorage: StringStorageApi

    @BeforeTest
    fun setup() = runTest {
        Emarsys.initialize()

        stringStorage = get<StringStorageApi>()
        Emarsys.enableTracking(AndroidEmarsysConfig("EMS11-C3FD3"))
    }

    @Test
    fun testRegisterPushToken() = runTest {
        stringStorage.put(PUSH_TOKEN_STORAGE_KEY, null)

        Emarsys.push.registerPushToken("testPushToken")

        stringStorage.get(PUSH_TOKEN_STORAGE_KEY) shouldNotBe null
    }

    @Test
    fun testClearPushToken() = runTest {
        stringStorage.put(PUSH_TOKEN_STORAGE_KEY, "testPushToken")

        Emarsys.push.clearPushToken()

        stringStorage.get(PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY) shouldBe null
    }

}
