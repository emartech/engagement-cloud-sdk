package com.emarsys.enable

import com.emarsys.TestEmarsysConfig
import com.emarsys.api.SdkState
import com.emarsys.config.SdkConfig
import com.emarsys.context.DefaultUrls
import com.emarsys.context.SdkContext
import com.emarsys.context.SdkContextApi
import com.emarsys.core.exceptions.SdkException.SdkAlreadyEnabledException
import com.emarsys.core.log.LogLevel
import com.emarsys.core.log.SdkLogger
import com.emarsys.core.state.StateMachineApi
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.di.SdkKoinIsolationContext.koin
import com.emarsys.enable.config.SdkConfigStoreApi
import com.emarsys.fake.FakeStringStorage
import com.emarsys.mobileengage.session.SessionApi
import com.emarsys.util.JsonUtil
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
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

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class EnableOrganizerTests: KoinTest {

    override fun getKoin(): Koin = koin

    private lateinit var testModule: Module

    private lateinit var mainDispatcher: CoroutineDispatcher

    private lateinit var mockMeStateMachine: StateMachineApi
    private lateinit var mockSdkConfigLoader: SdkConfigStoreApi<SdkConfig>
    private lateinit var mockSession: SessionApi
    private lateinit var sdkContext: SdkContextApi
    private lateinit var enableOrganizer: EnableOrganizerApi

    @BeforeTest
    fun setUp() {
        testModule = module {
            single<StringStorageApi> { FakeStringStorage() }
            single<Json> { JsonUtil.json }
        }
        koin.loadModules(listOf(testModule))

        mainDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(mainDispatcher)
        mockMeStateMachine = mock {
            everySuspend { activate() } returns Result.success(Unit)
        }
        mockSdkConfigLoader = mock(MockMode.autoUnit)
        mockSession = mock(MockMode.autoUnit)
        everySuspend { mockSdkConfigLoader.load() } returns null
        sdkContext = SdkContext(
            StandardTestDispatcher(),
            mainDispatcher,
            DefaultUrls("", "", "", "", "", ""),
            LogLevel.Error,
            mutableSetOf(),
            logBreadcrumbsQueueSize = 10
        )
        enableOrganizer = EnableOrganizer(
            mockMeStateMachine,
            sdkContext,
            mockSdkConfigLoader,
            mockSession,
            SdkLogger("TestLoggerName", mock(MockMode.autofill), sdkContext = mock())
        )
    }

    @AfterTest
    fun tearDown() {
        koin.unloadModules(listOf(testModule))
    }

    @Test
    fun enable_should_call_activate_onMeStateMachine_and_store_config_and_set_config_and_state_on_context() =
        runTest {
            val config = TestEmarsysConfig("testAppCode")

            enableOrganizer.enable(config)

            verifySuspend { mockSdkConfigLoader.store(config) }
            verifySuspend {
                mockMeStateMachine.activate()
            }
            sdkContext.config shouldBe config
            sdkContext.currentSdkState.value shouldBe SdkState.active
            verifySuspend { mockSession.startSession() }
        }

    @Test
    fun testEnableWithValidation_should_throw_whenAConfigWasAlreadyStored() =
        runTest {
            val config = TestEmarsysConfig("testAppCode")
            everySuspend { mockSdkConfigLoader.load() } returns config

            val exception = shouldThrow<SdkAlreadyEnabledException> {
                enableOrganizer.enableWithValidation(config)
            }
            exception.message shouldBe "Emarsys SDK was already enabled!"
            verifySuspend(VerifyMode.exactly(0)) {
                mockMeStateMachine.activate()
            }
        }

    @Test
    fun testEnableWithValidation_should_call_setup_whenNoConfigWasStored() =
        runTest {
            everySuspend { mockSdkConfigLoader.load() } returns null

            val config = TestEmarsysConfig("testAppCode")

            enableOrganizer.enableWithValidation(config)

            verifySuspend { mockSdkConfigLoader.store(config) }
            verifySuspend {
                mockMeStateMachine.activate()
            }
            sdkContext.config shouldBe config
            sdkContext.currentSdkState.value shouldBe SdkState.active
        }

    @Test
    fun testEnableWithValidation_should_throwException_whenStateMachineActivation_returnsFailure() =
        runTest {
            val testException = Exception("failure")
            everySuspend { mockSdkConfigLoader.load() } returns null
            everySuspend { mockMeStateMachine.activate() } returns Result.failure(testException)

            val config = TestEmarsysConfig("testAppCode")

            shouldThrow<Exception> { enableOrganizer.enableWithValidation(config) }

            verifySuspend { mockSdkConfigLoader.store(config) }
            verifySuspend {
                mockMeStateMachine.activate()
            }
            sdkContext.config shouldBe config
            sdkContext.currentSdkState.value shouldBe SdkState.onHold
            verifySuspend(VerifyMode.exactly(0)) { mockSession.startSession() }
        }
}