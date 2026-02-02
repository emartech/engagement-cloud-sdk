package com.emarsys.enable

import com.emarsys.TestEmarsysConfig
import com.emarsys.api.SdkState
import com.emarsys.config.SdkConfig
import com.emarsys.context.SdkContextApi
import com.emarsys.core.exceptions.SdkException.SdkAlreadyEnabledException
import com.emarsys.core.log.SdkLogger
import com.emarsys.core.state.StateMachineApi
import com.emarsys.enable.config.SdkConfigStoreApi
import com.emarsys.mobileengage.session.SessionApi
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class EnableOrganizerTests {
    private lateinit var testDispatcher: CoroutineDispatcher

    private lateinit var mockMeStateMachine: StateMachineApi
    private lateinit var mockSdkConfigStore: SdkConfigStoreApi<SdkConfig>
    private lateinit var mockSession: SessionApi
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var enableOrganizer: EnableOrganizerApi

    @BeforeTest
    fun setUp() {
        testDispatcher = StandardTestDispatcher()
        mockMeStateMachine = mock(MockMode.autoUnit)
        everySuspend { mockMeStateMachine.activate() } returns Result.success(Unit)

        mockSdkConfigStore = mock(MockMode.autoUnit)
        mockSession = mock(MockMode.autoUnit)
        everySuspend { mockSdkConfigStore.load() } returns null
        mockSdkContext = mock(MockMode.autofill)
        every { mockSdkContext.sdkDispatcher } returns testDispatcher

        enableOrganizer = EnableOrganizer(
            mockMeStateMachine,
            mockSdkContext,
            mockSdkConfigStore,
            mockSession,
            SdkLogger("TestLoggerName", mock(MockMode.autofill), sdkContext = mock())
        )
    }

    @Test
    fun enable_should_call_activate_onMeStateMachine_and_store_config_and_set_config_and_state_on_context() =
        runTest {
            val config = TestEmarsysConfig("testAppCode")

            enableOrganizer.enable(config)

            verifySuspend { mockSdkConfigStore.store(config) }
            verifySuspend { mockMeStateMachine.activate() }
            verify { mockSdkContext.config = config }
            verifySuspend { mockSdkContext.setSdkState(SdkState.OnHold) }
            verifySuspend { mockSdkContext.setSdkState(SdkState.Active) }
            verifySuspend { mockSession.startSession() }
        }

    @Test
    fun testEnableWithValidation_should_throw_whenAConfigWasAlreadyStored() =
        runTest {
            val config = TestEmarsysConfig("testAppCode")
            everySuspend { mockSdkConfigStore.load() } returns config

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
            everySuspend { mockSdkConfigStore.load() } returns null

            val config = TestEmarsysConfig("testAppCode")

            enableOrganizer.enableWithValidation(config)

            verifySuspend { mockSdkConfigStore.store(config) }
            verifySuspend {
                mockMeStateMachine.activate()
            }
            verify { mockSdkContext.config = config }
            verifySuspend { mockSdkContext.setSdkState(SdkState.OnHold) }
            verifySuspend { mockSdkContext.setSdkState(SdkState.Active) }
        }

    @Test
    fun testEnableWithValidation_should_throwException_whenStateMachineActivation_returnsFailure() =
        runTest {
            val testException = Exception("failure")
            everySuspend { mockSdkConfigStore.load() } returns null
            everySuspend { mockMeStateMachine.activate() } returns Result.failure(testException)

            val config = TestEmarsysConfig("testAppCode")

            shouldThrow<Exception> { enableOrganizer.enableWithValidation(config) }

            verifySuspend { mockSdkConfigStore.store(config) }
            verifySuspend {
                mockMeStateMachine.activate()
            }
            verify { mockSdkContext.config = config }
            verifySuspend { mockSdkContext.setSdkState(SdkState.OnHold) }
            verifySuspend(VerifyMode.exactly(0)) { mockSession.startSession() }
        }
}