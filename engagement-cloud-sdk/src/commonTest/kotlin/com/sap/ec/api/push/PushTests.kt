package com.sap.ec.api.push

import com.sap.ec.api.SdkState
import com.sap.ec.context.DefaultUrls
import com.sap.ec.context.SdkContext
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.log.LogLevel
import com.sap.ec.core.storage.StringStorageApi
import com.sap.ec.di.SdkKoinIsolationContext.koin
import com.sap.ec.fake.FakeStringStorage
import com.sap.ec.util.JsonUtil
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
import kotlinx.serialization.json.Json
import org.koin.core.Koin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.test.KoinTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PushTests: KoinTest {

    override fun getKoin(): Koin = koin

    private companion object {
        const val PUSH_TOKEN = "testPushToken"
    }

    private lateinit var testModule: Module

    private val mainDispatcher = StandardTestDispatcher()

    init {
        Dispatchers.setMain(mainDispatcher)
    }

    private lateinit var mockLoggingPush: PushInstance
    private lateinit var mockGathererPush: PushInstance
    private lateinit var mockPushInternal: PushInstance
    private lateinit var sdkContext: SdkContextApi
    private lateinit var push: Push<PushInstance, PushInstance, PushInstance>

    @BeforeTest
    fun setup() = runTest {
        testModule = module {
            single<StringStorageApi> { FakeStringStorage() }
            single<Json> { JsonUtil.json }
        }
        koin.loadModules(listOf(testModule))

        mockLoggingPush = mock()
        mockGathererPush = mock()
        mockPushInternal = mock()

        sdkContext = SdkContext(
            sdkDispatcher = StandardTestDispatcher(),
            mainDispatcher = mainDispatcher,
            defaultUrls = DefaultUrls("", "", "", "", "", "", ""),
            remoteLogLevel = LogLevel.Error,
            features = mutableSetOf(),
            logBreadcrumbsQueueSize = 10,
            onContactLinkingFailed = null
        )

        everySuspend { mockLoggingPush.activate() } returns Unit
        everySuspend { mockGathererPush.activate() } returns Unit
        everySuspend { mockPushInternal.activate() } returns Unit

        push =
            Push(mockLoggingPush, mockGathererPush, mockPushInternal, sdkContext)
        push.registerOnContext()
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
                PUSH_TOKEN
            )
        } returns Unit

        push.registerPushToken(PUSH_TOKEN)

        verifySuspend {
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

        sdkContext.setSdkState(SdkState.OnHold)
        push.registerPushToken(PUSH_TOKEN)

        verifySuspend {
            mockGathererPush.registerPushToken(
                PUSH_TOKEN
            )
        }
    }

    @Test
    fun testRegisterPushToken_activeState() = runTest {
        everySuspend {
            mockPushInternal.registerPushToken(
                PUSH_TOKEN
            )
        } returns Unit

        sdkContext.setSdkState(SdkState.Active)
        push.registerPushToken(PUSH_TOKEN)

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

        sdkContext.setSdkState(SdkState.Active)
        val result = push.registerPushToken(PUSH_TOKEN)

        result.onFailure {
            it shouldBe expectedException
        }
    }

    @Test
    fun testClearPushToken_inactiveState() = runTest {
        everySuspend {
            mockLoggingPush.clearPushToken()
        } returns Unit

        push.clearPushToken()

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
        push.clearPushToken()

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
        push.clearPushToken()

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
        val result = push.clearPushToken()

        result.onFailure {
            it shouldBe expectedException
        }
    }

    @Test
    fun testPushToken_inactiveState() = runTest {
        everySuspend {
            mockLoggingPush.getPushToken()
        } returns null

        val result = push.getPushToken()

        result.onSuccess {
            it shouldBe null
        }
    }

    @Test
    fun testPushToken_onHoldState() = runTest {
        everySuspend {
            mockGathererPush.getPushToken()
        } returns PUSH_TOKEN

        sdkContext.setSdkState(SdkState.OnHold)

        val result = push.getPushToken()

        result.onSuccess {
            it shouldBe PUSH_TOKEN
        }
    }

    @Test
    fun testPushToken_activeState() = runTest {
        everySuspend {
            mockPushInternal.getPushToken()
        } returns PUSH_TOKEN

        sdkContext.setSdkState(SdkState.Active)

        val result = push.getPushToken()

        result.onSuccess {
            it shouldBe PUSH_TOKEN
        }
    }
}
