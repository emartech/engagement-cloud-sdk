package com.emarsys.api.push

import com.emarsys.core.storage.TypedStorageApi
import com.emarsys.networking.clients.push.PushClientApi
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.BeforeTest
import kotlin.test.Test

class PushInternalTests : TestsWithMocks() {
    private companion object {
        const val PUSH_TOKEN = "testPushToken"
    }

    @Mock
    lateinit var mockPushClient: PushClientApi

    @Mock
    lateinit var mockStorage: TypedStorageApi<String?>

    private lateinit var pushInternal: PushInternal

    override fun setUpMocks() = injectMocks(mocker)

    @BeforeTest
    fun setup() = runTest {
        pushInternal = PushInternal(mockPushClient, mockStorage)
    }

    @Test
    fun testRegisterPushToken_shouldNotDoAnything_whenPushTokenStoredAlready() = runTest {
        every { mockStorage.get("emsPushToken") } returns PUSH_TOKEN

        pushInternal.registerPushToken(PUSH_TOKEN)

        verifyWithSuspend(exhaustive = false) {
            repeat(0) {
                mockPushClient.registerPushToken(PUSH_TOKEN)
            }
        }
    }

    @Test
    fun testRegisterPushToken_shouldRegisterPushToken_whenPushTokenWasNotStoredPreviously() = runTest {
        every { mockStorage.get("emsPushToken") } returns null
        every { mockStorage.put("emsPushToken", PUSH_TOKEN) } returns Unit
        everySuspending { mockPushClient.registerPushToken(isAny()) } returns Unit

        pushInternal.registerPushToken(PUSH_TOKEN)

        verifyWithSuspend(exhaustive = false) {
            mockPushClient.registerPushToken(PUSH_TOKEN)
            mockStorage.put("emsPushToken", PUSH_TOKEN)
        }
    }

    @Test
    fun testRegisterPushToken_shouldRegisterPushToken_whenStoredPushTokenIsDifferent() = runTest {
        every { mockStorage.get("emsPushToken") } returns "<differentTestPushToken>"
        every { mockStorage.put("emsPushToken", PUSH_TOKEN) } returns Unit
        everySuspending { mockPushClient.registerPushToken(isAny()) } returns Unit

        pushInternal.registerPushToken(PUSH_TOKEN)

        verifyWithSuspend(exhaustive = false) {
            mockPushClient.registerPushToken(PUSH_TOKEN)
            mockStorage.put("emsPushToken", PUSH_TOKEN)
        }
    }

    @Test
    fun testClearPushToken() = runTest {
        everySuspending { mockPushClient.clearPushToken() } returns Unit

        pushInternal.clearPushToken()

        verifyWithSuspend(exhaustive = false) {
            mockPushClient.clearPushToken()
        }
    }

    @Test
    fun testPushToken() = runTest {
        every { mockStorage.get(PushConstants.PUSH_TOKEN_STORAGE_KEY) } returns PUSH_TOKEN
        pushInternal.registerPushToken(PUSH_TOKEN)
        pushInternal.pushToken shouldBe PUSH_TOKEN
    }

}
