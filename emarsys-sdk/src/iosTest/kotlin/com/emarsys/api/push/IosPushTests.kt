package com.emarsys.api.push

import com.emarsys.api.SdkState
import com.emarsys.context.DefaultUrls
import com.emarsys.context.SdkContext
import com.emarsys.context.SdkContextApi
import com.emarsys.core.exceptions.PreconditionFailedException
import com.emarsys.core.log.LogLevel
import com.emarsys.mobileengage.action.models.BasicAppEventActionModel
import com.emarsys.mobileengage.action.models.BasicOpenExternalUrlActionModel
import com.emarsys.mobileengage.push.BasicPushUserInfo
import com.emarsys.mobileengage.push.BasicPushUserInfoEms
import com.emarsys.mobileengage.push.IosPush
import com.emarsys.mobileengage.push.IosPushInstance
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.exactly
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class IosPushTests {
    private companion object {
        const val PUSH_TOKEN = "testPushToken"
        const val SID = "testSID"
        const val CAMPAIGN_ID = "testCampaignId"
        val USER_INFO_MAP = mapOf(
            "ems" to mapOf(
                "sid" to SID,
                "multichannelId" to CAMPAIGN_ID,
                "actions" to listOf(
                    mapOf(
                        "type" to "OpenExternalUrl",
                        "url" to "https://www.emarsys.com"
                    ),
                    mapOf(
                        "type" to "MEAppEvent",
                        "name" to "name",
                        "payload" to mapOf("key" to "value")
                    )
                )
            )
        )
        val PUSH_USER_INFO = BasicPushUserInfo(
            ems = BasicPushUserInfoEms(
                sid = SID,
                multichannelId = CAMPAIGN_ID,
                actions = listOf(
                    BasicOpenExternalUrlActionModel("https://www.emarsys.com"),
                    BasicAppEventActionModel("name", mapOf("key" to "value"))
                )
            )
        )
    }

    private lateinit var mockLoggingPush: IosPushInstance
    private lateinit var mockGathererPush: IosPushInstance
    private lateinit var mockPushInternal: IosPushInstance
    private lateinit var testUNUserNotificationCenterDelegateProtocol: UNUserNotificationCenterDelegateProtocol
    private lateinit var sdkContext: SdkContextApi
    private lateinit var iosPush: IosPush<IosPushInstance, IosPushInstance, IosPushInstance>

    @BeforeTest
    fun setup() = runTest {
        val sdkDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(sdkDispatcher)

        mockLoggingPush = mock()
        mockGathererPush = mock()
        mockPushInternal = mock()
        testUNUserNotificationCenterDelegateProtocol = TestUserNotificationCenterDelegate()

        sdkContext = SdkContext(
            sdkDispatcher,
            StandardTestDispatcher(),
            DefaultUrls("", "", "", "", "", "", ""),
            LogLevel.Error,
            mutableSetOf()
        )

        everySuspend { mockLoggingPush.activate() } returns Unit
        everySuspend { mockGathererPush.activate() } returns Unit
        everySuspend { mockPushInternal.activate() } returns Unit

        iosPush = IosPush(mockLoggingPush, mockGathererPush, mockPushInternal, sdkContext)
        iosPush.registerOnContext()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testRegisterPushToken_inactiveState() = runTest {
        everySuspend {
            mockLoggingPush.registerPushToken(
                PUSH_TOKEN
            )
        } returns Unit

        iosPush.registerPushToken(PUSH_TOKEN)

        verifySuspend(exactly(1)) {
            mockLoggingPush.registerPushToken(
                PUSH_TOKEN
            )
        }
    }

    @Test
    fun testRegisterPushToken_onHoldState() = runTest {
        everySuspend {
            mockGathererPush.registerPushToken(
                PUSH_TOKEN
            )
        } returns Unit

        sdkContext.setSdkState(SdkState.onHold)
        iosPush.registerPushToken(PUSH_TOKEN)

        verifySuspend { mockGathererPush.registerPushToken(PUSH_TOKEN) }
    }

    @Test
    fun testRegisterPushToken_activeState() = runTest {
        everySuspend {
            mockPushInternal.registerPushToken(
                PUSH_TOKEN
            )
        } returns Unit

        sdkContext.setSdkState(SdkState.active)
        iosPush.registerPushToken(PUSH_TOKEN)

        verifySuspend {
            mockPushInternal.registerPushToken(
                PUSH_TOKEN
            )
        }
    }

    @Test
    fun testRegisterPushToken_activeState_when_throws() = runTest {
        val expectedException = Exception()
        everySuspend {
            mockPushInternal.registerPushToken(
                PUSH_TOKEN
            )
        } throws expectedException

        sdkContext.setSdkState(SdkState.active)
        val result = iosPush.registerPushToken(PUSH_TOKEN)

        result.isFailure shouldBe true
        result.exceptionOrNull() shouldBe expectedException
    }

    @Test
    fun testClearPushToken_inactiveState() = runTest {
        everySuspend {
            mockLoggingPush.clearPushToken()
        } returns Unit

        iosPush.clearPushToken()

        verifySuspend {
            mockLoggingPush.clearPushToken()
        }
    }

    @Test
    fun testClearPushToken_onHoldState() = runTest {
        everySuspend {
            mockGathererPush.clearPushToken()
        } returns Unit

        sdkContext.setSdkState(SdkState.onHold)
        iosPush.clearPushToken()

        verifySuspend {
            mockGathererPush.clearPushToken()
        }
    }

    @Test
    fun testClearPushToken_activeState() = runTest {
        everySuspend {
            mockPushInternal.clearPushToken()
        } returns Unit

        sdkContext.setSdkState(SdkState.active)
        iosPush.clearPushToken()

        verifySuspend {
            mockPushInternal.clearPushToken()
        }
    }

    @Test
    fun testClearPushToken_activeState_when_throws() = runTest {
        val expectedException = Exception()
        everySuspend {
            mockPushInternal.clearPushToken()
        } throws expectedException

        sdkContext.setSdkState(SdkState.active)
        val result = iosPush.clearPushToken()

        result.isFailure shouldBe true
        result.exceptionOrNull() shouldBe expectedException
    }

    @Test
    fun testPushToken_inactiveState() = runTest {
        every {
            mockLoggingPush.pushToken
        } returns null

        val result = iosPush.pushToken

        result.onSuccess {
            it shouldBe null
        }
    }

    @Test
    fun testPushToken_onHoldState() = runTest {
        every {
            mockGathererPush.pushToken
        } returns PUSH_TOKEN

        sdkContext.setSdkState(SdkState.onHold)

        val result = iosPush.pushToken

        result.onSuccess {
            it shouldBe PUSH_TOKEN
        }
    }

    @Test
    fun testPushToken_activeState() = runTest {
        every {
            mockPushInternal.pushToken
        } returns PUSH_TOKEN

        sdkContext.setSdkState(SdkState.active)

        val result = iosPush.pushToken

        result.onSuccess {
            it shouldBe PUSH_TOKEN
        }
    }

    @Test
    fun testCustomerUserNotificationCenterDelegate_inactiveState() = runTest {
        every {
            mockLoggingPush.customerUserNotificationCenterDelegate
        } returns null

        val result = iosPush.customerUserNotificationCenterDelegate

        result.onSuccess {
            it shouldBe null
        }
    }

    @Test
    fun testCustomerUserNotificationCenterDelegate_onHoldState() = runTest {
        every {
            mockGathererPush.customerUserNotificationCenterDelegate
        } returns testUNUserNotificationCenterDelegateProtocol

        sdkContext.setSdkState(SdkState.onHold)
        val result = iosPush.customerUserNotificationCenterDelegate

        result.onSuccess {
            it shouldBe testUNUserNotificationCenterDelegateProtocol
        }
    }

    @Test
    fun testCustomerUserNotificationCenterDelegate_onActiveState() = runTest {
        every {
            mockPushInternal.customerUserNotificationCenterDelegate
        } returns testUNUserNotificationCenterDelegateProtocol

        sdkContext.setSdkState(SdkState.active)
        val result = iosPush.customerUserNotificationCenterDelegate

        result.onSuccess {
            it shouldBe testUNUserNotificationCenterDelegateProtocol
        }
    }

    @Test
    fun testEmarsysUserNotificationCenterDelegate_inactiveState() = runTest {
        every {
            mockLoggingPush.emarsysUserNotificationCenterDelegate
        } returns testUNUserNotificationCenterDelegateProtocol

        val result = iosPush.customerUserNotificationCenterDelegate

        result.onSuccess {
            it shouldBe null
        }
    }

    @Test
    fun testEmarsysUserNotificationCenterDelegate_onHoldState() = runTest {
        every {
            mockGathererPush.emarsysUserNotificationCenterDelegate
        } returns testUNUserNotificationCenterDelegateProtocol

        sdkContext.setSdkState(SdkState.onHold)
        val result = iosPush.customerUserNotificationCenterDelegate

        result.onSuccess {
            it shouldBe testUNUserNotificationCenterDelegateProtocol
        }
    }

    @Test
    fun testEmarsysUserNotificationCenterDelegate_onActiveState() = runTest {
        every {
            mockPushInternal.emarsysUserNotificationCenterDelegate
        } returns testUNUserNotificationCenterDelegateProtocol

        sdkContext.setSdkState(SdkState.active)
        val result = iosPush.customerUserNotificationCenterDelegate

        result.onSuccess {
            it shouldBe testUNUserNotificationCenterDelegateProtocol
        }
    }

    @Test
    fun testHandleSilentMessageWithUserInfo_inactiveState() = runTest {
        everySuspend {
            mockLoggingPush.handleSilentMessageWithUserInfo(any())
        } returns Unit

        iosPush.handleSilentMessageWithUserInfo(USER_INFO_MAP)

        verifySuspend {
            mockLoggingPush.handleSilentMessageWithUserInfo(PUSH_USER_INFO)
        }
    }

    @Test
    fun testHandleSilentMessageWithUserInfo_onHoldState() = runTest {
        everySuspend {
            mockGathererPush.handleSilentMessageWithUserInfo(any())
        } returns Unit

        sdkContext.setSdkState(SdkState.onHold)
        iosPush.handleSilentMessageWithUserInfo(USER_INFO_MAP)

        verifySuspend { mockGathererPush.handleSilentMessageWithUserInfo(PUSH_USER_INFO) }
    }

    @Test
    fun testHandleSilentMessageWithUserInfo_onActiveState() = runTest {
        everySuspend {
            mockPushInternal.handleSilentMessageWithUserInfo(any())
        } returns Unit

        sdkContext.setSdkState(SdkState.active)
        iosPush.handleSilentMessageWithUserInfo(USER_INFO_MAP)

        verifySuspend { mockPushInternal.handleSilentMessageWithUserInfo(PUSH_USER_INFO) }
    }

    @Test
    fun testHandleSilentMessageWithUserInfo_onActiveState_whenUserInfoDeSerializationFails() =
        runTest {
            val invalidUserInfoMap = mapOf("key" to "value")
            everySuspend {
                mockPushInternal.handleSilentMessageWithUserInfo(any())
            } returns Unit

            sdkContext.setSdkState(SdkState.active)
            val result = iosPush.handleSilentMessageWithUserInfo(invalidUserInfoMap)

            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe PreconditionFailedException("Error while handling silent push message, the userInfo can't be parsed")
            verifySuspend(exactly(0)) { mockPushInternal.handleSilentMessageWithUserInfo(any()) }
        }
}
