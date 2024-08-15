package com.emarsys.api.inbox

import com.emarsys.api.SdkState
import com.emarsys.api.inbox.model.Message
import com.emarsys.context.DefaultUrls
import com.emarsys.context.SdkContext
import com.emarsys.context.SdkContextApi
import com.emarsys.core.log.LogLevel
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InboxTests {
    private companion object {
        const val TAG = "testTag"
        const val MESSAGE_ID = "testMessageId"
        val testMessage = Message("testId", "testCampaignId", null,"testTitle", "testBody", null,123456789,null,null,null,null,null)
        val testException = Exception()
    }

    private val mainDispatcher = StandardTestDispatcher()

    init {
        Dispatchers.setMain(mainDispatcher)
    }

    private lateinit var mockLoggingInbox: InboxInstance
    private lateinit var mockGathererInbox: InboxInstance
    private lateinit var mockInboxInternal: InboxInstance
    private lateinit var sdkContext: SdkContextApi
    private lateinit var inbox: Inbox<InboxInstance, InboxInstance, InboxInstance>

    @BeforeTest
    fun setup() = runTest {
        mockLoggingInbox = mock()
        mockGathererInbox = mock()
        mockInboxInternal = mock()
        
        sdkContext = SdkContext(
            StandardTestDispatcher(),
            mainDispatcher,
            DefaultUrls("", "", "", "", "", "", ""),
            LogLevel.Error,
            mutableSetOf()
        )

        everySuspend { mockLoggingInbox.activate() } returns Unit
        everySuspend { mockGathererInbox.activate() } returns Unit
        everySuspend { mockInboxInternal.activate() } returns Unit

        inbox = Inbox(mockLoggingInbox, mockGathererInbox, mockInboxInternal, sdkContext)

        inbox.registerOnContext()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testFetchMessages_shouldCallLoggingInstance_whenInactive() = runTest {
        val expectedResult = emptyList<Message>()
        everySuspend { mockLoggingInbox.fetchMessages() } returns expectedResult

        val result = inbox.fetchMessages()

        result.getOrNull() shouldBe expectedResult
        verifySuspend { mockLoggingInbox.fetchMessages() }
    }

    @Test
    fun testFetchMessages_shouldCallGathererInstance_whenOnHold() = runTest {
        val expectedResult = listOf(testMessage)
        everySuspend { mockGathererInbox.fetchMessages() } returns expectedResult

        sdkContext.setSdkState(SdkState.onHold)

        val result = inbox.fetchMessages()

        verifySuspend { mockGathererInbox.fetchMessages() }
        result.getOrNull() shouldBe expectedResult
    }

    @Test
    fun testFetchMessages_shouldCallGathererInstance_whenActive() = runTest {
        val expectedResult = listOf(testMessage)
        everySuspend { mockInboxInternal.fetchMessages() } returns expectedResult

        sdkContext.setSdkState(SdkState.active)

        val result = inbox.fetchMessages()

        verifySuspend { mockInboxInternal.fetchMessages() }
        result.getOrNull() shouldBe expectedResult
    }

    @Test
    fun testFetchMessages_shouldCallGathererInstance_whenActive_throws() = runTest {
        everySuspend { mockInboxInternal.fetchMessages() } throws testException

        sdkContext.setSdkState(SdkState.active)

        val result = inbox.fetchMessages()

        result.exceptionOrNull() shouldBe testException
    }

    @Test
    fun testAddTag_shouldCallLoggingInstance_whenInactive() = runTest {
        everySuspend { mockLoggingInbox.addTag(TAG, MESSAGE_ID) } returns Unit

        inbox.addTag(TAG, MESSAGE_ID)

        verifySuspend { mockLoggingInbox.addTag(TAG, MESSAGE_ID) }
    }

    @Test
    fun testAddTag_shouldCallGathererInstance_whenOnHold() = runTest {
        everySuspend { mockGathererInbox.addTag(TAG, MESSAGE_ID) } returns Unit

        sdkContext.setSdkState(SdkState.onHold)

        inbox.addTag(TAG, MESSAGE_ID)

        verifySuspend { mockGathererInbox.addTag(TAG, MESSAGE_ID) }
    }

    @Test
    fun testAddTag_shouldCallGathererInstance_whenActive() = runTest {
        everySuspend { mockInboxInternal.addTag(TAG, MESSAGE_ID) } returns Unit

        sdkContext.setSdkState(SdkState.active)

        inbox.addTag(TAG, MESSAGE_ID)

        verifySuspend { mockInboxInternal.addTag(TAG, MESSAGE_ID) }
    }

    @Test
    fun testAddTag_shouldCallGathererInstance_whenActive_throws() = runTest {
        everySuspend { mockInboxInternal.addTag(TAG, MESSAGE_ID) } throws testException

        sdkContext.setSdkState(SdkState.active)

        val result = inbox.addTag(TAG, MESSAGE_ID)

        result.exceptionOrNull() shouldBe testException
    }

    @Test
    fun testRemoveTag_shouldCallLoggingInstance_whenInactive() = runTest {
        everySuspend { mockLoggingInbox.removeTag(TAG, MESSAGE_ID) } returns Unit

        inbox.removeTag(TAG, MESSAGE_ID)

        verifySuspend { mockLoggingInbox.removeTag(TAG, MESSAGE_ID) }
    }

    @Test
    fun testRemoveTag_shouldCallGathererInstance_whenOnHold() = runTest {
        everySuspend { mockGathererInbox.removeTag(TAG, MESSAGE_ID) } returns Unit

        sdkContext.setSdkState(SdkState.onHold)

        inbox.removeTag(TAG, MESSAGE_ID)

        verifySuspend { mockGathererInbox.removeTag(TAG, MESSAGE_ID) }
    }

    @Test
    fun testRemoveTag_shouldCallGathererInstance_whenActive() = runTest {
        everySuspend { mockInboxInternal.removeTag(TAG, MESSAGE_ID) } returns Unit

        sdkContext.setSdkState(SdkState.active)

        inbox.removeTag(TAG, MESSAGE_ID)

        verifySuspend { mockInboxInternal.removeTag(TAG, MESSAGE_ID) }
    }

    @Test
    fun testRemoveTag_shouldCallGathererInstance_whenActive_throws() = runTest {
        everySuspend { mockInboxInternal.removeTag(TAG, MESSAGE_ID) } throws testException

        sdkContext.setSdkState(SdkState.active)

        val result = inbox.removeTag(TAG, MESSAGE_ID)

        result.exceptionOrNull() shouldBe testException
    }
}