package com.emarsys.api.push

import com.emarsys.networking.clients.push.PushClientApi
import com.emarsys.core.storage.StorageApi
import kotlinx.coroutines.test.runTest
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.BeforeTest
import kotlin.test.Test

class PushInternalTests: TestsWithMocks() {

    @Mock
    lateinit var mockPushClient: PushClientApi

    @Mock
    lateinit var mockStorage: StorageApi<String>

    private lateinit var pushInternal: PushInternal

    override fun setUpMocks() = injectMocks(mocker)

    @BeforeTest
    fun setup() = runTest {
        pushInternal = PushInternal(mockPushClient, mockStorage)
    }

    @Test
    fun testRegisterPushToken_shouldNotDoAnything_whenPushTokenStoredAlready() = runTest {
        every { mockStorage.get("emsPushToken") } returns "<testPushToken>"

        pushInternal.registerPushToken("<testPushToken>")

        verifyWithSuspend(exhaustive = false) {
            repeat(0) {
                mockPushClient.registerPushToken("<testPushToken>")
            }
        }
    }

    @Test
    fun testRegisterPushToken_shouldRegisterPushToken_whenPushTokenWasNotStoredPreviously() = runTest {
        every { mockStorage.get("emsPushToken") } returns null
        every { mockStorage.put("emsPushToken", "<testPushToken>") } returns Unit
        everySuspending { mockPushClient.registerPushToken(isAny()) } returns Unit

        pushInternal.registerPushToken("<testPushToken>")

        verifyWithSuspend(exhaustive = false) {
            mockPushClient.registerPushToken("<testPushToken>")
            mockStorage.put("emsPushToken", "<testPushToken>")
        }
    }

    @Test
    fun testRegisterPushToken_shouldRegisterPushToken_whenStoredPushTokenIsDifferent() = runTest {
        every { mockStorage.get("emsPushToken") } returns "<differentTestPushToken>"
        every { mockStorage.put("emsPushToken", "<testPushToken>") } returns Unit
        everySuspending { mockPushClient.registerPushToken(isAny()) } returns Unit

        pushInternal.registerPushToken("<testPushToken>")

        verifyWithSuspend(exhaustive = false) {
            mockPushClient.registerPushToken("<testPushToken>")
            mockStorage.put("emsPushToken", "<testPushToken>")
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

}
