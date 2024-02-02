package com.emarsys.mobileengage.action

import com.emarsys.api.AppEvent
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class ActionCommandFactoryTests {

    private companion object {
        const val ID = "testId"
        const val TITLE = "testTitle"
        const val TYPE = "testType"
        const val NAME = "testName"
        val payload = mapOf("testKey" to "testValue")
    }


    @Test
    fun testCreate_shouldCreateAppEventCommand() = runTest {
        val action = AppEventActionModel(ID, TITLE, TYPE, NAME, payload)
        val result = com.emarsys.mobileengage.action.ActionCommandFactory().create(action)

        (result is AppEventCommand) shouldBe true
        val events = MutableSharedFlow<AppEvent>()
        launch {
            (result as AppEventCommand).invoke(events)
        }

        events.first() shouldBe AppEvent(NAME, payload)
    }
}