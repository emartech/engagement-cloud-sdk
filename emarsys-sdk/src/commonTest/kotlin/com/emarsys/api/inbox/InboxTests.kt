package com.emarsys.api.inbox

import com.emarsys.api.SdkState
import com.emarsys.api.inbox.model.Message
import com.emarsys.context.DefaultUrls
import com.emarsys.context.SdkContext
import com.emarsys.context.SdkContextApi
import com.emarsys.core.log.LogLevel
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InboxTests : TestsWithMocks() {
    private companion object {
        const val TAG = "testTag"
        const val MESSAGE_ID = "testMessageId"
        val testMessage = Message("testId", "testCampaignId", null,"testTitle", "testBody", null,123456789,null,null,null,null,null)
        val testException = Exception()
    }

    override fun setUpMocks() = injectMocks(mocker)

    @Mock
    lateinit var mockLoggingInbox: InboxInstance

    @Mock
    lateinit var mockGathererInbox: InboxInstance

    @Mock
    lateinit var mockInboxInternal: InboxInstance

    private lateinit var sdkContext: SdkContextApi

    private lateinit var inbox: Inbox<InboxInstance, InboxInstance, InboxInstance>

    init {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @BeforeTest
    fun setup() = runTest {
        sdkContext = SdkContext(
            StandardTestDispatcher(),
            DefaultUrls("", "", "", "", "", "", ""),
            LogLevel.Error,
            mutableSetOf()
        )

        everySuspending { mockLoggingInbox.activate() } returns Unit
        everySuspending { mockGathererInbox.activate() } returns Unit
        everySuspending { mockInboxInternal.activate() } returns Unit

        inbox = Inbox(mockLoggingInbox, mockGathererInbox, mockInboxInternal, sdkContext)

        inbox.registerOnContext()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        mocker.reset()
    }

    @Test
    fun testFetchMessages_shouldCallLoggingInstance_whenInactive() = runTest {
        val expectedResult = emptyList<Message>()
        everySuspending { mockLoggingInbox.fetchMessages() } returns expectedResult

        val result = inbox.fetchMessages()

        result.getOrNull() shouldBe expectedResult
        verifyWithSuspend(exhaustive = false) { mockLoggingInbox.fetchMessages() }
    }

    @Test
    fun testFetchMessages_shouldCallGathererInstance_whenOnHold() = runTest {
        val expectedResult = listOf(testMessage)
        everySuspending { mockGathererInbox.fetchMessages() } returns expectedResult

        sdkContext.setSdkState(SdkState.onHold)

        val result = inbox.fetchMessages()

        verifyWithSuspend(exhaustive = false) { mockGathererInbox.fetchMessages() }
        result.getOrNull() shouldBe expectedResult
    }

    @Test
    fun testFetchMessages_shouldCallGathererInstance_whenActive() = runTest {
        val expectedResult = listOf(testMessage)
        everySuspending { mockInboxInternal.fetchMessages() } returns expectedResult

        sdkContext.setSdkState(SdkState.active)

        val result = inbox.fetchMessages()

        verifyWithSuspend(exhaustive = false) { mockInboxInternal.fetchMessages() }
        result.getOrNull() shouldBe expectedResult
    }

    @Test
    fun testFetchMessages_shouldCallGathererInstance_whenActive_throws() = runTest {
        everySuspending { mockInboxInternal.fetchMessages() } runs {
            throw testException
        }

        sdkContext.setSdkState(SdkState.active)

        val result = inbox.fetchMessages()

        result.exceptionOrNull() shouldBe testException
    }

    @Test
    fun testAddTag_shouldCallLoggingInstance_whenInactive() = runTest {
        everySuspending { mockLoggingInbox.addTag(TAG, MESSAGE_ID) } returns Unit

        inbox.addTag(TAG, MESSAGE_ID)

        verifyWithSuspend(exhaustive = false) { mockLoggingInbox.addTag(TAG, MESSAGE_ID) }
    }

    @Test
    fun testAddTag_shouldCallGathererInstance_whenOnHold() = runTest {
        everySuspending { mockGathererInbox.addTag(TAG, MESSAGE_ID) } returns Unit

        sdkContext.setSdkState(SdkState.onHold)

        inbox.addTag(TAG, MESSAGE_ID)

        verifyWithSuspend(exhaustive = false) { mockGathererInbox.addTag(TAG, MESSAGE_ID) }
    }

    @Test
    fun testAddTag_shouldCallGathererInstance_whenActive() = runTest {
        everySuspending { mockInboxInternal.addTag(TAG, MESSAGE_ID) } returns Unit

        sdkContext.setSdkState(SdkState.active)

        inbox.addTag(TAG, MESSAGE_ID)

        verifyWithSuspend(exhaustive = false) { mockInboxInternal.addTag(TAG, MESSAGE_ID) }
    }

    @Test
    fun testAddTag_shouldCallGathererInstance_whenActive_throws() = runTest {
        everySuspending { mockInboxInternal.addTag(TAG, MESSAGE_ID) } runs {
            throw testException
        }

        sdkContext.setSdkState(SdkState.active)

        val result = inbox.addTag(TAG, MESSAGE_ID)

        result.exceptionOrNull() shouldBe testException
    }

    @Test
    fun testRemoveTag_shouldCallLoggingInstance_whenInactive() = runTest {
        everySuspending { mockLoggingInbox.removeTag(TAG, MESSAGE_ID) } returns Unit

        inbox.removeTag(TAG, MESSAGE_ID)

        verifyWithSuspend(exhaustive = false) { mockLoggingInbox.removeTag(TAG, MESSAGE_ID) }
    }

    @Test
    fun testRemoveTag_shouldCallGathererInstance_whenOnHold() = runTest {
        everySuspending { mockGathererInbox.removeTag(TAG, MESSAGE_ID) } returns Unit

        sdkContext.setSdkState(SdkState.onHold)

        inbox.removeTag(TAG, MESSAGE_ID)

        verifyWithSuspend(exhaustive = false) { mockGathererInbox.removeTag(TAG, MESSAGE_ID) }
    }

    @Test
    fun testRemoveTag_shouldCallGathererInstance_whenActive() = runTest {
        everySuspending { mockInboxInternal.removeTag(TAG, MESSAGE_ID) } returns Unit

        sdkContext.setSdkState(SdkState.active)

        inbox.removeTag(TAG, MESSAGE_ID)

        verifyWithSuspend(exhaustive = false) { mockInboxInternal.removeTag(TAG, MESSAGE_ID) }
    }

    @Test
    fun testRemoveTag_shouldCallGathererInstance_whenActive_throws() = runTest {
        everySuspending { mockInboxInternal.removeTag(TAG, MESSAGE_ID) } runs {
            throw testException
        }

        sdkContext.setSdkState(SdkState.active)

        val result = inbox.removeTag(TAG, MESSAGE_ID)

        result.exceptionOrNull() shouldBe testException
    }
}