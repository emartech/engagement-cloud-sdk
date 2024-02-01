package com.emarsys.action

import com.emarsys.api.oneventaction.OnEventActionInternalApi
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.test.runTest
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.Test

class OnEventActionFactoryTests : TestsWithMocks() {
    private companion object {
        const val ID = "testId"
        const val TITLE = "testTitle"
        const val TYPE = "testType"
        const val NAME = "testName"
        val payload = mapOf("testKey" to "testValue")
    }

    override fun setUpMocks() = injectMocks(mocker)

    @Mock
    lateinit var mockActionCommandFactory: ActionCommandFactoryApi

    @Mock
    lateinit var mockOnEventActionInternal: OnEventActionInternalApi

    @Mock
    lateinit var mockAppEventCommand: AppEventCommand

    private val onEventActionFactory: OnEventActionFactory by withMocks {
        OnEventActionFactory(
            mockActionCommandFactory,
            mockOnEventActionInternal
        )
    }

    @Test
    fun testCreate_withAppEventActionModel() = runTest {
        val action = AppEventActionModel(ID, TITLE, TYPE, NAME, payload)

        everySuspending { mockActionCommandFactory.create(action) } returns mockAppEventCommand

        val result = onEventActionFactory.create(action)

        result shouldNotBe null

    }

    @Test
    fun testCreate_withCustomEventActionModel() = runTest {
        val action = CustomEventActionModel(ID, TITLE, TYPE, NAME, payload)
        everySuspending { mockActionCommandFactory.create(action) } returns mockAppEventCommand

        val result = onEventActionFactory.create(action)

        result shouldNotBe null
    }

    @Test
    fun testCreate_withInvalidActionModel() = runTest {
        val action = DismissActionModel(ID, TITLE, TYPE)


        val exception = shouldThrow<IllegalArgumentException> {
            onEventActionFactory.create(action)

        }
        exception.message shouldBe "Action is not an OnEventAction"
    }
}