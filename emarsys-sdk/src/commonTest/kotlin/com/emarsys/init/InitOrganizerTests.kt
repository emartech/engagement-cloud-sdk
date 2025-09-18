package com.emarsys.init

import com.emarsys.api.SdkState
import com.emarsys.context.DefaultUrls
import com.emarsys.context.SdkContext
import com.emarsys.context.SdkContextApi
import com.emarsys.core.log.LogLevel
import com.emarsys.core.log.SdkLogger
import com.emarsys.core.state.StateMachineApi
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.di.SdkKoinIsolationContext.koin
import com.emarsys.fake.FakeStringStorage
import com.emarsys.util.JsonUtil
import dev.mokkery.MockMode
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
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
class InitOrganizerTests : KoinTest {

    override fun getKoin(): Koin = koin

    private lateinit var testModule: Module

    private val mainDispatcher = StandardTestDispatcher()

    init {
        Dispatchers.setMain(mainDispatcher)
    }

    private lateinit var mockStateMachine: StateMachineApi
    private lateinit var sdkContext: SdkContextApi
    private lateinit var initOrganizer: InitOrganizer

    @BeforeTest
    fun setUp() {
        testModule = module {
            single<StringStorageApi> { FakeStringStorage() }
            single<Json> { JsonUtil.json }
        }
        koin.loadModules(listOf(testModule))

        mockStateMachine = mock()
        sdkContext = SdkContext(
            StandardTestDispatcher(),
            mainDispatcher,
            DefaultUrls("", "", "", "", "", ""),
            LogLevel.Error,
            mutableSetOf(),
            logBreadcrumbsQueueSize = 10
        )
        initOrganizer = InitOrganizer(
            mockStateMachine,
            sdkContext,
            SdkLogger("TestLoggerName", mock(MockMode.autofill), sdkContext = mock())
        )
    }

    @AfterTest
    fun tearDown() {
        koin.unloadModules(listOf(testModule))
    }

    @Test
    fun init_should_call_activate_onStateMachine_and_set_config_and_state_on_context() =
        runTest {
            everySuspend { mockStateMachine.activate() } returns Result.success(Unit)

            initOrganizer.init()

            verifySuspend {
                mockStateMachine.activate()
            }
            sdkContext.currentSdkState.value shouldBe SdkState.initialized
        }

    @Test
    fun init_should_not_move_sdkState_backwards_whenInitStateMachine_alreadyActivatedTheSDK() =
        runTest {
            everySuspend { mockStateMachine.activate() } calls {
                sdkContext.setSdkState(SdkState.active)
                Result.success(Unit)
            }

            initOrganizer.init()

            verifySuspend {
                mockStateMachine.activate()
            }
            sdkContext.currentSdkState.value shouldBe SdkState.active
        }

    @Test
    fun init_should_throwException_ifStateMachineActivation_throws() =
        runTest {
            val testException = Exception("failure")
            everySuspend { mockStateMachine.activate() } returns Result.failure(testException)

            val exception = shouldThrow<Exception> { initOrganizer.init() }

            verifySuspend {
                mockStateMachine.activate()
            }
            sdkContext.currentSdkState.value shouldBe SdkState.inactive
            exception shouldBe testException
        }
}
