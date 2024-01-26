package com.emarsys.api.push

import com.emarsys.core.storage.StorageApi
import com.emarsys.core.storage.StringFakeStorage
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class GathererPushTests {
    companion object {
        const val PUSH_TOKEN = "testPushToken"
    }

    private lateinit var pushContext: PushContext
    private lateinit var pushGatherer: PushGatherer
    private lateinit var storage: StorageApi<String?>

    @BeforeTest
    fun setup() {
        storage = StringFakeStorage()
        pushContext = PushContext()
        pushGatherer = PushGatherer(pushContext, storage)
    }

    @Test
    fun testGathering() = runTest {
        val setPushToken = PushCall.SetPushToken(PUSH_TOKEN)
        val clearPushToken = PushCall.ClearPushToken()

        val expected = listOf(
            setPushToken,
            clearPushToken
        )

        pushGatherer.registerPushToken(PUSH_TOKEN)
        pushGatherer.clearPushToken()

        pushContext.calls shouldBe expected
    }

    @Test
    fun testPushToken() = runTest {
        storage.put(PushConstants.PUSH_TOKEN_STORAGE_KEY, PUSH_TOKEN)
        pushGatherer.pushToken shouldBe PUSH_TOKEN
    }

}