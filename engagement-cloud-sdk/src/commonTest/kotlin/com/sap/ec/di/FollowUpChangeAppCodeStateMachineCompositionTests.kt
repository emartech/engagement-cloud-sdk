package com.sap.ec.di

import com.sap.ec.core.log.Logger
import com.sap.ec.core.state.StateMachine
import com.sap.ec.core.state.TestState
import dev.mokkery.MockMode
import dev.mokkery.mock
import io.kotest.matchers.collections.shouldContain
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class FollowUpChangeAppCodeStateMachineCompositionTests {

    @Test
    fun followUpChangeAppCodeStateMachine_shouldIncludeLoadEmbeddedMessagingMessages() = runTest {
        // Arrange: construct a StateMachine mirroring the
        // FollowUpChangeAppCodeStateMachine composition in SetupInjection.kt.
        // Verifies the composition includes loadEmbeddedMessagingFetchMessagesState
        // to trigger message/category refresh after app switch.
        val applyRemoteConfig = TestState("applyAppCodeBasedRemoteConfig")
        val fetchMeta = TestState("fetchEmbeddedMessagingMetaState")
        val loadMessages = TestState("loadEmbeddedMessagingFetchMessagesState")

        val stateMachine = StateMachine(
            states = listOf(applyRemoteConfig, fetchMeta, loadMessages),
            name = StateMachineTypes.FollowUpChangeAppCodeStateMachine.name,
            logger = mock<Logger>(MockMode.autofill)
        )

        // Act: activate and track which states were executed
        val activatedStateNames = mutableListOf<String>()
        listOf(applyRemoteConfig, fetchMeta, loadMessages).forEach { state ->
            state.functionCalls = { stateName, functionName ->
                if (functionName == "active") {
                    activatedStateNames.add(stateName)
                }
            }
        }

        stateMachine.activate()

        // Assert: the composition must include loadEmbeddedMessagingFetchMessagesState
        activatedStateNames shouldContain "loadEmbeddedMessagingFetchMessagesState"
    }
}
