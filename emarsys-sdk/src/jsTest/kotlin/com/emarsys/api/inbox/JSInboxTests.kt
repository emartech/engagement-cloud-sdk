package com.emarsys.api.inbox

import com.emarsys.api.inbox.model.Message
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.await
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class JSInboxTests {
    private companion object {
        const val TEST_TAG = "testTag"
        const val TEST_MESSAGE_ID = "testId"
        val mockMessage1: Message = Message(
            id = "message1",
            campaignId = "campaign1",
            title = "title1",
            body = "body1",
            receivedAt = 12345
        )
        val mockMessage2: Message = Message(
            id = "message2",
            campaignId = "campaign2",
            title = "title2",
            body = "body2",
            receivedAt = 12345789
        )
        val testMessages = listOf(mockMessage1, mockMessage2)
    }

    private lateinit var jsInbox: JSInboxApi
    private lateinit var mockInboxApi: InboxApi

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        mockInboxApi = mock()
        jsInbox = JSInbox(mockInboxApi, TestScope())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun fetchMessages_shouldCall_fetchMessages_onInboxApi() = runTest {
        everySuspend { mockInboxApi.fetchMessages() } returns Result.success(testMessages)

        val result = jsInbox.fetchMessages().await()

        result shouldBe testMessages
    }

    @Test
    fun fetchMessages_shouldThrowException_ifFetchMessages_fails() = runTest {
        everySuspend { mockInboxApi.fetchMessages() } returns Result.failure(Exception())

        shouldThrow<Exception> { jsInbox.fetchMessages().await() }
    }

    @Test
    fun addTag_shouldCall_addTag_onInboxApi() = runTest {
        everySuspend { mockInboxApi.addTag(TEST_TAG, TEST_MESSAGE_ID) } returns Result.success(Unit)

        jsInbox.addTag(TEST_TAG, TEST_MESSAGE_ID).await()

        verifySuspend { mockInboxApi.addTag(TEST_TAG, TEST_MESSAGE_ID) }
    }

    @Test
    fun addTag_shouldThrowException_ifAddTag_fails() = runTest {
        everySuspend { mockInboxApi.addTag(TEST_TAG, TEST_MESSAGE_ID) } returns Result.failure(Exception())

        shouldThrow<Exception> { jsInbox.addTag(TEST_TAG, TEST_MESSAGE_ID).await() }
    }

    @Test
    fun removeTag_shouldCall_removeTag_onInboxApi() = runTest {
        everySuspend { mockInboxApi.removeTag(TEST_TAG, TEST_MESSAGE_ID) } returns Result.success(Unit)

        jsInbox.removeTag(TEST_TAG, TEST_MESSAGE_ID).await()

        verifySuspend { mockInboxApi.removeTag(TEST_TAG, TEST_MESSAGE_ID) }
    }

    @Test
    fun removeTag_shouldThrowException_ifRemoveTag_fails() = runTest {
        everySuspend { mockInboxApi.removeTag(TEST_TAG, TEST_MESSAGE_ID) } returns Result.failure(Exception())

        shouldThrow<Exception> { jsInbox.removeTag(TEST_TAG, TEST_MESSAGE_ID).await() }
    }
}