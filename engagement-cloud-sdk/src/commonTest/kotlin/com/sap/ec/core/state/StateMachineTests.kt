package com.sap.ec.core.state

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

internal class TestState(override val name: String) : State {

    var functionCalls: ((stateName: String, functionName: String) -> Unit)? = null
    override fun prepare() {
        functionCalls?.invoke(name, this::prepare.name)
    }

    override suspend fun active(): Result<Unit> {
        functionCalls?.invoke(name, this::active.name)
        return Result.success(Unit)
    }

    override fun relax() {
        functionCalls?.invoke(name, this::relax.name)
    }

}
internal class StateMachineTests {

    companion object {
        val state1 = TestState("state1")
        val state2 = TestState("state2")
        val state3 = TestState("state3")
    }

    lateinit var stateMachine: StateMachine

    @BeforeTest
    fun setup() = runTest {
        stateMachine = StateMachine(listOf(state1, state2, state3))
    }

    @Test
    fun testActivate() = runTest {
        val excepted = listOf(
            "state1 - prepare",
            "state1 - active",
            "state1 - relax",
            "state2 - prepare",
            "state2 - active",
            "state2 - relax",
            "state3 - prepare",
            "state3 - active",
            "state3 - relax"
        )

        val functionCalls = mutableListOf<String>()

        state1.functionCalls = { name, function ->
            functionCalls.add("$name - $function")
        }
        state2.functionCalls = { name, function ->
            functionCalls.add("$name - $function")
        }
        state3.functionCalls = { name, function ->
            functionCalls.add("$name - $function")
        }

        stateMachine.activate()

        functionCalls shouldBe excepted
    }

}
