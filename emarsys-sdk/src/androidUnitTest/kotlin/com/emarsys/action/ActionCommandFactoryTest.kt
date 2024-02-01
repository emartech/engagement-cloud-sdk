package com.emarsys.action

import android.content.Context
import com.emarsys.Emarsys
import com.emarsys.EmarsysSdkInitializer
import com.emarsys.api.AppEvent
import com.emarsys.applicationContext
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.BeforeTest


class ActionCommandFactoryTest {
    private companion object {
        const val ID = "testId"
        const val TITLE = "testTitle"
        const val TYPE = "testType"
        const val NAME = "testName"
        val payload = mapOf("testKey" to "testValue")
    }

    @BeforeTest
    fun setUp() {
        val mockContext: Context = mockk(relaxed = true)
        every { mockContext.applicationContext } returns mockContext
        EmarsysSdkInitializer().create(mockContext)
    }

    @Test
    fun testCreate_shouldCreateAppEventCommand() = runTest {
        val action = AppEventActionModel(ID, TITLE, TYPE, NAME, payload)
        val result = ActionCommandFactory().create(action)

        (result is AppEventCommand) shouldBe true
        val events = MutableSharedFlow<AppEvent>()
        launch {
            (result as AppEventCommand).invoke(events)
        }

        events.first() shouldBe AppEvent(applicationContext, NAME, payload)
    }
}