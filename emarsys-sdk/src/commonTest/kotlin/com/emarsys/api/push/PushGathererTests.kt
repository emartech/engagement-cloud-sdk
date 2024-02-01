package com.emarsys.api.push

import com.emarsys.api.AppEvent
import com.emarsys.api.push.PushConstants.PUSH_TOKEN_STORAGE_KEY
import com.emarsys.core.storage.TypedStorageApi
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.Test

class PushGathererTests : TestsWithMocks() {

    private companion object {
        const val PUSH_TOKEN = "testPushToken"
        val registerPushToken = PushCall.RegisterPushToken(PUSH_TOKEN)
        val clearPushToken = PushCall.ClearPushToken()

        val expected = mutableListOf(
            registerPushToken,
            clearPushToken
        )
    }

    override fun setUpMocks() = injectMocks(mocker)

    @Mock
    lateinit var mockStringStorage: TypedStorageApi<String?>

    private lateinit var pushContext: PushContext
    private val notificationEvents: MutableSharedFlow<AppEvent> = MutableSharedFlow()
    private val pushGatherer: PushGatherer by withMocks {
        pushContext = PushContext(expected)
        PushGatherer(pushContext, mockStringStorage, notificationEvents)
    }

    @Test
    fun testGathering() = runTest {
        pushGatherer.registerPushToken(PUSH_TOKEN)
        pushGatherer.clearPushToken()

        pushContext.calls shouldBe expected
    }

    @Test
    fun testPushToken() = runTest {
        every { mockStringStorage.get(PUSH_TOKEN_STORAGE_KEY) } returns PUSH_TOKEN

        pushGatherer.pushToken shouldBe PUSH_TOKEN
    }

}