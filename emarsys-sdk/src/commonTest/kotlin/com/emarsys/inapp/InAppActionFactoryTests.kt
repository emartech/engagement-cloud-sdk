package com.emarsys.inapp

import com.emarsys.action.*
import com.emarsys.api.inapp.InAppInternalApi
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.test.runTest
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.Test

class InAppActionFactoryTests : TestsWithMocks() {
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
    lateinit var mockInAppInternal: InAppInternalApi

    @Mock
    lateinit var mockAppEventCommand: AppEventCommand

    @Mock
    lateinit var mockAskForPushPermissionCommand: AskForPushPermissionCommand

    @Mock
    lateinit var mockOpenExternalUrlCommand: OpenExternalUrlCommand

    @Mock
    lateinit var mockDismissCommand: DismissCommand

    private val inAppActionFactory: InAppActionFactoryApi by withMocks {
        InAppActionFactory(
            mockActionCommandFactory,
            mockInAppInternal
        )
    }

    @Test
    fun testCreate_withAppEventActionModel() = runTest {
        val action = AppEventActionModel(ID, TITLE, TYPE, NAME, payload)

        everySuspending { mockActionCommandFactory.create(action) } returns mockAppEventCommand

        val result = inAppActionFactory.create(action)

        result shouldNotBe null
    }

    @Test
    fun testCreate_withCustomEventActionModel() = runTest {
        val action = CustomEventActionModel(ID, TITLE, TYPE, NAME, payload)
        everySuspending { mockActionCommandFactory.create(action) } returns mockAppEventCommand

        val result = inAppActionFactory.create(action)

        result shouldNotBe null
    }

    @Test
    fun testCreate_withDismissActionModel() = runTest {
        val action = DismissActionModel(ID, TITLE, TYPE)

        everySuspending { mockActionCommandFactory.create(action) } returns mockDismissCommand

        val result = inAppActionFactory.create(action)

        result shouldNotBe null
    }

    @Test
    fun testCreate_withOpenExternalUrlActionModel() = runTest {
        val action = OpenExternalUrlActionModel(ID, TITLE, TYPE, "https://www.emarsys.com")

        everySuspending { mockActionCommandFactory.create(action) } returns mockOpenExternalUrlCommand

        val result = inAppActionFactory.create(action)

        result shouldNotBe null
    }

    @Test
    fun testCreate_withAskForPushPermissionActionModel() = runTest {
        val action = AskForPushPermissionActionModel(ID, TITLE, TYPE)

        everySuspending { mockActionCommandFactory.create(action) } returns mockAskForPushPermissionCommand

        val result = inAppActionFactory.create(action)

        result shouldNotBe null
    }

    @Test
    fun testCreate_withInvalidActionModel() = runTest {
        val action = UnknownActionModel()

        val exception = shouldThrow<IllegalArgumentException> {
            inAppActionFactory.create(action)

        }
        exception.message shouldBe "Action is not an InAppAction"
    }
}