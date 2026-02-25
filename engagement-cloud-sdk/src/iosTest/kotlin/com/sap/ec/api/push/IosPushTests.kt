package com.sap.ec.api.push

import com.sap.ec.api.SdkState
import com.sap.ec.context.DefaultUrls
import com.sap.ec.context.SdkContext
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.exceptions.SdkException.PreconditionFailedException
import com.sap.ec.core.log.LogLevel
import com.sap.ec.core.log.Logger
import com.sap.ec.core.storage.StringStorageApi
import com.sap.ec.di.SdkKoinIsolationContext.koin
import com.sap.ec.fake.FakeStringStorage
import com.sap.ec.mobileengage.action.models.BasicAppEventActionModel
import com.sap.ec.mobileengage.action.models.BasicOpenExternalUrlActionModel
import com.sap.ec.mobileengage.push.IosPushInstance
import com.sap.ec.mobileengage.push.IosPushWrapper
import com.sap.ec.util.JsonUtil
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
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import org.koin.core.Koin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.test.KoinTest
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class IosPushTests: KoinTest {

    override fun getKoin(): Koin = koin

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

    private lateinit var testModule: Module

    private lateinit var mockLoggingPush: IosPushInstance
    private lateinit var mockGathererPush: IosPushInstance
    private lateinit var mockPushInternal: IosPushInstance
    private lateinit var mockSdkLogger: Logger
    private lateinit var testUNUserNotificationCenterDelegateProtocol: UNUserNotificationCenterDelegateProtocol
    private lateinit var sdkContext: SdkContextApi
    private lateinit var iosPushWrapper: IosPushWrapper<IosPushInstance, IosPushInstance, IosPushInstance>

    @BeforeTest
    fun setup() = runTest {
        testModule = module {
            single<StringStorageApi> { FakeStringStorage() }
            single<Json> { JsonUtil.json }
        }
        koin.loadModules(listOf(testModule))

        val sdkDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(sdkDispatcher)

        mockLoggingPush = mock()
        mockGathererPush = mock()
        mockPushInternal = mock()
        mockSdkLogger = mock(MockMode.autofill)
        everySuspend { mockSdkLogger.error(any(), any<Throwable>()) } returns Unit

        testUNUserNotificationCenterDelegateProtocol =
            TestUserNotificationCenterDelegate()

        sdkContext = SdkContext(
            sdkDispatcher = StandardTestDispatcher(),
            mainDispatcher = StandardTestDispatcher(),
            defaultUrls = DefaultUrls("", "", "", "", "", "", "", ""),
            remoteLogLevel = LogLevel.Error,
            features = mutableSetOf(),
            logBreadcrumbsQueueSize = 10,
            onContactLinkingFailed = null
        )

        everySuspend { mockLoggingPush.activate() } returns Unit
        everySuspend { mockGathererPush.activate() } returns Unit
        everySuspend { mockPushInternal.activate() } returns Unit

        iosPushWrapper =
            IosPushWrapper(mockLoggingPush, mockGathererPush, mockPushInternal, sdkContext, mockSdkLogger)
        iosPushWrapper.registerOnContext()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        koin.unloadModules(listOf(testModule))
    }

    @Test
    fun testRegisterPushToken_inactiveState() = runTest {
        everySuspend {
            mockLoggingPush.registerPushToken(
                IosPushTests.Companion.PUSH_TOKEN
            )
        } returns Unit

        iosPushWrapper.registerPushToken(IosPushTests.Companion.PUSH_TOKEN)

        verifySuspend(exactly(1)) {
            mockLoggingPush.registerPushToken(
                IosPushTests.Companion.PUSH_TOKEN
            )
        }
    }

    @Test
    fun testRegisterPushToken_onHoldState() = runTest {
        everySuspend {
            mockGathererPush.registerPushToken(
                IosPushTests.Companion.PUSH_TOKEN
            )
        } returns Unit

        sdkContext.setSdkState(SdkState.OnHold)
        iosPushWrapper.registerPushToken(IosPushTests.Companion.PUSH_TOKEN)

        verifySuspend { mockGathererPush.registerPushToken(IosPushTests.Companion.PUSH_TOKEN) }
    }

    @Test
    fun testRegisterPushToken_activeState() = runTest {
        everySuspend {
            mockPushInternal.registerPushToken(
                IosPushTests.Companion.PUSH_TOKEN
            )
        } returns Unit

        sdkContext.setSdkState(SdkState.Active)
        iosPushWrapper.registerPushToken(IosPushTests.Companion.PUSH_TOKEN)

        verifySuspend {
            mockPushInternal.registerPushToken(
                IosPushTests.Companion.PUSH_TOKEN
            )
        }
    }

    @Test
    fun testRegisterPushToken_activeState_when_throws() = runTest {
        val expectedException = Exception()
        everySuspend {
            mockPushInternal.registerPushToken(
                IosPushTests.Companion.PUSH_TOKEN
            )
        } throws expectedException

        sdkContext.setSdkState(SdkState.Active)
        val result = iosPushWrapper.registerPushToken(IosPushTests.Companion.PUSH_TOKEN)

        result.isFailure shouldBe true
        result.exceptionOrNull() shouldBe expectedException
    }

    @Test
    fun testClearPushToken_inactiveState() = runTest {
        everySuspend {
            mockLoggingPush.clearPushToken()
        } returns Unit

        iosPushWrapper.clearPushToken()

        verifySuspend {
            mockLoggingPush.clearPushToken()
        }
    }

    @Test
    fun testClearPushToken_onHoldState() = runTest {
        everySuspend {
            mockGathererPush.clearPushToken()
        } returns Unit

        sdkContext.setSdkState(SdkState.OnHold)
        iosPushWrapper.clearPushToken()

        verifySuspend {
            mockGathererPush.clearPushToken()
        }
    }

    @Test
    fun testClearPushToken_activeState() = runTest {
        everySuspend {
            mockPushInternal.clearPushToken()
        } returns Unit

        sdkContext.setSdkState(SdkState.Active)
        iosPushWrapper.clearPushToken()

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

        sdkContext.setSdkState(SdkState.Active)
        val result = iosPushWrapper.clearPushToken()

        result.isFailure shouldBe true
        result.exceptionOrNull() shouldBe expectedException
    }

    @Test
    fun testPushToken_inactiveState() = runTest {
        everySuspend {
            mockLoggingPush.getPushToken()
        } returns null

        val result = iosPushWrapper.getPushToken()

        result.onSuccess {
            it shouldBe null
        }
    }

    @Test
    fun testPushToken_onHoldState() = runTest {
        everySuspend {
            mockGathererPush.getPushToken()
        } returns IosPushTests.Companion.PUSH_TOKEN

        sdkContext.setSdkState(SdkState.OnHold)

        val result = iosPushWrapper.getPushToken()

        result.onSuccess {
            it shouldBe IosPushTests.Companion.PUSH_TOKEN
        }
    }

    @Test
    fun testPushToken_activeState() = runTest {
        everySuspend {
            mockPushInternal.getPushToken()
        } returns IosPushTests.Companion.PUSH_TOKEN

        sdkContext.setSdkState(SdkState.Active)

        val result = iosPushWrapper.getPushToken()

        result.onSuccess {
            it shouldBe IosPushTests.Companion.PUSH_TOKEN
        }
    }

    @Test
    fun testUserNotificationCenterDelegate_inactiveState() = runTest {
        every {
            mockLoggingPush.userNotificationCenterDelegate
        } returns testUNUserNotificationCenterDelegateProtocol

        val result = iosPushWrapper.userNotificationCenterDelegate

        result shouldBe null
    }

    @Test
    fun testUserNotificationCenterDelegate_onHoldState() = runTest {
        every {
            mockGathererPush.userNotificationCenterDelegate
        } returns testUNUserNotificationCenterDelegateProtocol

        sdkContext.setSdkState(SdkState.OnHold)

        advanceUntilIdle()

        val result = iosPushWrapper.userNotificationCenterDelegate

        result shouldBe testUNUserNotificationCenterDelegateProtocol
    }

    @Test
    fun testUserNotificationCenterDelegate_onActiveState() = runTest {
        every {
            mockPushInternal.userNotificationCenterDelegate
        } returns testUNUserNotificationCenterDelegateProtocol

        sdkContext.setSdkState(SdkState.Active)

        advanceUntilIdle()

        val result = iosPushWrapper.userNotificationCenterDelegate

        result shouldBe testUNUserNotificationCenterDelegateProtocol
    }

    @Test
    fun testHandleSilentMessageWithUserInfo_inactiveState() = runTest {
        everySuspend {
            mockLoggingPush.handleSilentMessageWithUserInfo(any())
        } returns Unit

        iosPushWrapper.handleSilentMessageWithUserInfo(IosPushTests.Companion.SILENT_USER_INFO_MAP)

        verifySuspend {
            mockLoggingPush.handleSilentMessageWithUserInfo(IosPushTests.Companion.SILENT_PUSH_USER_INFO)
        }
    }

    @Test
    fun testHandleSilentMessageWithUserInfo_onHoldState() = runTest {
        everySuspend {
            mockGathererPush.handleSilentMessageWithUserInfo(any())
        } returns Unit

        sdkContext.setSdkState(SdkState.OnHold)
        iosPushWrapper.handleSilentMessageWithUserInfo(IosPushTests.Companion.SILENT_USER_INFO_MAP)

        verifySuspend { mockGathererPush.handleSilentMessageWithUserInfo(IosPushTests.Companion.SILENT_PUSH_USER_INFO) }
    }

    @Test
    fun testHandleSilentMessageWithUserInfo_onActiveState() = runTest {
        everySuspend {
            mockPushInternal.handleSilentMessageWithUserInfo(any())
        } returns Unit

        sdkContext.setSdkState(SdkState.Active)
        iosPushWrapper.handleSilentMessageWithUserInfo(IosPushTests.Companion.SILENT_USER_INFO_MAP)

        verifySuspend { mockPushInternal.handleSilentMessageWithUserInfo(IosPushTests.Companion.SILENT_PUSH_USER_INFO) }
    }

    @Test
    fun testHandleSilentMessageWithUserInfo_onActiveState_whenUserInfoDeSerializationFails() =
        runTest {
            val invalidUserInfoMap = mapOf("key" to "value")
            everySuspend {
                mockPushInternal.handleSilentMessageWithUserInfo(any())
            } returns Unit

            sdkContext.setSdkState(SdkState.Active)
            val result = iosPushWrapper.handleSilentMessageWithUserInfo(invalidUserInfoMap)

            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe PreconditionFailedException("Error while handling silent push message, the userInfo can't be parsed")
            verifySuspend { mockSdkLogger.error(any(), any<Throwable>()) }
            verifySuspend(exactly(0)) { mockPushInternal.handleSilentMessageWithUserInfo(any()) }
        }
}
