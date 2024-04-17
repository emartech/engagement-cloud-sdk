package com.emarsys.api.inbox

import com.emarsys.api.inbox.model.Message
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class GathererInboxTests {

    private companion object {
        const val TAG = "testTag"
        const val MESSAGE_ID = "testMessageId"
        val testMessages = mutableListOf(Message("testId", "testCampaignId", null,"testTitle", "testBody", null,123456789,null,null,null,null,null))
        val testInboxCalls = mutableListOf<InboxCall>()
    }

    private lateinit var inboxContext: InboxContext

    private lateinit var gathererInbox: GathererInbox

    @BeforeTest
    fun setup() = runTest {
        inboxContext = InboxContext(testInboxCalls, testMessages)
        gathererInbox = GathererInbox(inboxContext)
    }

    @Test
    fun testFetchMessages_shouldAddCallToContext_andReturnStoredCalls() = runTest {
        val testCall = InboxCall.FetchMessages()
        val result = gathererInbox.fetchMessages()

        inboxContext.calls.contains(testCall) shouldBe true
        result shouldBe testMessages
    }

    @Test
    fun testAddTag_shouldAddCallToContext() = runTest {
        val testCall = InboxCall.AddTag(TAG, MESSAGE_ID)
        gathererInbox.addTag(TAG, MESSAGE_ID)

        inboxContext.calls.contains(testCall) shouldBe true
    }

    @Test
    fun testRemoveTag_shouldAddCallToContext() = runTest {
        val testCall = InboxCall.RemoveTag(TAG, MESSAGE_ID)
        gathererInbox.removeTag(TAG, MESSAGE_ID)

        inboxContext.calls.contains(testCall) shouldBe true
    }
}