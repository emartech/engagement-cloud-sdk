package com.sap.ec.api.push

import com.sap.ec.api.SdkState
import com.sap.ec.config.SdkConfig
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.exceptions.SdkException.PreconditionFailedException
import com.sap.ec.core.log.LogConfigHolderApi
import com.sap.ec.core.log.Logger
import com.sap.ec.enable.config.SdkConfigStoreApi
import com.sap.ec.mobileengage.action.models.BasicAppEventActionModel
import com.sap.ec.mobileengage.action.models.BasicOpenExternalUrlActionModel
import com.sap.ec.mobileengage.push.IosPushInstance
import com.sap.ec.mobileengage.push.IosPushWrapper
import dev.mokkery.MockMode
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class IosPushWrapperTests {
    private companion object {
        const val PUSH_TOKEN = "testPushToken"
        const val VERSION = "APNS_V2"
        const val TRACKING_INFO = """{"trackingInfo":"testTrackingInfo"}"""
        const val REPORTING = """{"reportingKey":"reportingValue"}"""
        const val REPORTING2 = """{"reportingKey2":"reportingValue2"}"""
        const val NAME = "testName"
        val PAYLOAD = mapOf("key" to "value")
        const val URL_STRING = "https://www.sap.com"
        val SILENT_USER_INFO_MAP = buildMap {
            put(
                "ems", mapOf(
                    "version" to VERSION,
                    "trackingInfo" to TRACKING_INFO
                )
            )
            put("notification", buildMap {
                put(
                    "actions", listOf(
                        mapOf(
                            "type" to "OpenExternalUrl",
                            "reporting" to REPORTING,
                            "url" to URL_STRING
                        ),
                        mapOf(
                            "type" to "MEAppEvent",
                            "reporting" to REPORTING2,
                            "name" to NAME,
                            "payload" to PAYLOAD
                        )
                    )
                )
            })
        }
        val SILENT_PUSH_USER_INFO = SilentPushUserInfo(
            ems = Ems(
                version = VERSION,
                trackingInfo = TRACKING_INFO
            ),
            SilentNotification(
                silent = false,
                actions = listOf(
                    BasicOpenExternalUrlActionModel(REPORTING, URL_STRING),
                    BasicAppEventActionModel(REPORTING2, NAME, PAYLOAD)
                )
            )
        )
    }

    private lateinit var mockLoggingPush: IosPushInstance
    private lateinit var mockGathererPush: IosPushInstance
    private lateinit var mockPushInternal: IosPushInstance
    private lateinit var mockSdkLogger: Logger
    private lateinit var mockSdkConfigStore: SdkConfigStoreApi<SdkConfig>
    private lateinit var mockRemoteLogLevelHolder: LogConfigHolderApi
    private lateinit var testUNUserNotificationCenterDelegateProtocol: UNUserNotificationCenterDelegateProtocol
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var iosPushWrapper: IosPushWrapper<IosPushInstance, IosPushInstance, IosPushInstance>

    @BeforeTest
    fun setup() = runTest {
        val sdkDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(sdkDispatcher)

        mockLoggingPush = mock(MockMode.autofill)
        mockGathererPush = mock(MockMode.autofill)
        everySuspend { mockGathererPush.activate() } returns Unit
        mockPushInternal = mock(MockMode.autofill)
        mockSdkConfigStore = mock(MockMode.autofill)
        mockRemoteLogLevelHolder = mock(MockMode.autofill)
        mockSdkLogger = mock(MockMode.autofill)
        everySuspend { mockSdkLogger.error(any(), any<Throwable>()) } returns Unit

        testUNUserNotificationCenterDelegateProtocol =
            TestUserNotificationCenterDelegate()

        mockSdkContext = mock(MockMode.autofill)
        every { mockSdkContext.sdkDispatcher } returns sdkDispatcher

        iosPushWrapper =
            IosPushWrapper(
                mockLoggingPush,
                mockGathererPush,
                mockPushInternal,
                mockSdkContext,
                mockSdkLogger
            )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testRegisterPushToken_inactiveState() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Initialized)

        iosPushWrapper.registerOnContext()

        iosPushWrapper.registerPushToken(PUSH_TOKEN)

        verifySuspend(exactly(1)) { mockLoggingPush.registerPushToken(PUSH_TOKEN) }
    }

    @Test
    fun testRegisterPushToken_onHoldState() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.OnHold)

        iosPushWrapper.registerOnContext()

        iosPushWrapper.registerPushToken(PUSH_TOKEN)

        verifySuspend { mockGathererPush.registerPushToken(PUSH_TOKEN) }
    }

    @Test
    fun testRegisterPushToken_activeState() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Active)

        iosPushWrapper.registerOnContext()

        iosPushWrapper.registerPushToken(PUSH_TOKEN)

        verifySuspend { mockPushInternal.registerPushToken(PUSH_TOKEN) }
    }

    @Test
    fun testRegisterPushToken_activeState_when_throws() = runTest {
        val expectedException = Exception()
        everySuspend { mockPushInternal.registerPushToken(PUSH_TOKEN) } throws expectedException
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Active)

        iosPushWrapper.registerOnContext()

        val result = iosPushWrapper.registerPushToken(PUSH_TOKEN)

        result.isFailure shouldBe true
        result.exceptionOrNull() shouldBe expectedException
    }

    @Test
    fun testClearPushToken_inactiveState() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Initialized)

        iosPushWrapper.registerOnContext()

        iosPushWrapper.clearPushToken()

        verifySuspend { mockLoggingPush.clearPushToken() }
    }

    @Test
    fun testClearPushToken_onHoldState() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.OnHold)

        iosPushWrapper.registerOnContext()

        iosPushWrapper.clearPushToken()

        verifySuspend { mockGathererPush.clearPushToken() }
    }

    @Test
    fun testClearPushToken_activeState() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Active)

        iosPushWrapper.registerOnContext()

        iosPushWrapper.clearPushToken()

        verifySuspend { mockPushInternal.clearPushToken() }
    }

    @Test
    fun testClearPushToken_activeState_when_throws() = runTest {
        val expectedException = Exception()
        everySuspend {
            mockPushInternal.clearPushToken()
        } throws expectedException
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Active)

        iosPushWrapper.registerOnContext()

        val result = iosPushWrapper.clearPushToken()

        result.isFailure shouldBe true
        result.exceptionOrNull() shouldBe expectedException
    }

    @Test
    fun testPushToken_inactiveState() = runTest {
        everySuspend { mockLoggingPush.getPushToken() } returns null
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Initialized)

        iosPushWrapper.registerOnContext()

        val result = iosPushWrapper.getPushToken()

        result.onSuccess { it shouldBe null }
    }

    @Test
    fun testPushToken_onHoldState() = runTest {
        everySuspend { mockGathererPush.getPushToken() } returns PUSH_TOKEN
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.OnHold)

        iosPushWrapper.registerOnContext()

        val result = iosPushWrapper.getPushToken()

        result.onSuccess { it shouldBe PUSH_TOKEN }
    }

    @Test
    fun testPushToken_activeState() = runTest {
        everySuspend { mockPushInternal.getPushToken() } returns PUSH_TOKEN
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Active)

        iosPushWrapper.registerOnContext()

        val result = iosPushWrapper.getPushToken()

        result.onSuccess { it shouldBe PUSH_TOKEN }
    }

    @Test
    fun testUserNotificationCenterDelegate_inactiveState() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Initialized)
        every { mockLoggingPush.userNotificationCenterDelegate } returns testUNUserNotificationCenterDelegateProtocol

        iosPushWrapper.registerOnContext()

        val result = iosPushWrapper.userNotificationCenterDelegate

        result shouldBe null
    }

    @Test
    fun testUserNotificationCenterDelegate_onHoldState() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.OnHold)
        every { mockGathererPush.userNotificationCenterDelegate } returns testUNUserNotificationCenterDelegateProtocol

        iosPushWrapper.registerOnContext()

        val result = iosPushWrapper.userNotificationCenterDelegate

        result shouldBe testUNUserNotificationCenterDelegateProtocol
    }

    @Test
    fun testUserNotificationCenterDelegate_onActiveState() = runTest {
        every { mockPushInternal.userNotificationCenterDelegate } returns testUNUserNotificationCenterDelegateProtocol
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Active)

        iosPushWrapper.registerOnContext()

        val result = iosPushWrapper.userNotificationCenterDelegate

        result shouldBe testUNUserNotificationCenterDelegateProtocol
    }

    @Test
    fun testHandleSilentMessageWithUserInfo_inactiveState() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Initialized)

        iosPushWrapper.registerOnContext()

        iosPushWrapper.handleSilentMessageWithUserInfo(SILENT_USER_INFO_MAP)

        verifySuspend { mockLoggingPush.handleSilentMessageWithUserInfo(SILENT_PUSH_USER_INFO) }
    }

    @Test
    fun testHandleSilentMessageWithUserInfo_onHoldState() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.OnHold)
        iosPushWrapper.handleSilentMessageWithUserInfo(SILENT_USER_INFO_MAP)

        iosPushWrapper.registerOnContext()

        iosPushWrapper.handleSilentMessageWithUserInfo(SILENT_USER_INFO_MAP)

        verifySuspend { mockGathererPush.handleSilentMessageWithUserInfo(SILENT_PUSH_USER_INFO) }
    }

    @Test
    fun testHandleSilentMessageWithUserInfo_onActiveState() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Active)
        iosPushWrapper.handleSilentMessageWithUserInfo(SILENT_USER_INFO_MAP)

        iosPushWrapper.registerOnContext()

        iosPushWrapper.handleSilentMessageWithUserInfo(SILENT_USER_INFO_MAP)

        verifySuspend { mockPushInternal.handleSilentMessageWithUserInfo(SILENT_PUSH_USER_INFO) }
    }

    @Test
    fun testHandleSilentMessageWithUserInfo_onActiveState_whenUserInfoDeSerializationFails() =
        runTest {
            val invalidUserInfoMap = mapOf("key" to "value")
            every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Active)

            iosPushWrapper.registerOnContext()

            val result = iosPushWrapper.handleSilentMessageWithUserInfo(invalidUserInfoMap)

            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe PreconditionFailedException("Error while handling silent push message, the userInfo can't be parsed")
            verifySuspend { mockSdkLogger.error(any(), any<Throwable>()) }
            verifySuspend(exactly(0)) { mockPushInternal.handleSilentMessageWithUserInfo(any()) }
        }
}
