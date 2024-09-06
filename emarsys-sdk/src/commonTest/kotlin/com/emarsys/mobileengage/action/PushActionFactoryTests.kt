package com.emarsys.mobileengage.action

import com.emarsys.core.pushtoinapp.PushToInAppHandlerApi
import com.emarsys.mobileengage.action.actions.CustomEventAction
import com.emarsys.mobileengage.action.actions.PushToInappAction
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.action.models.InternalPushToInappActionModel
import com.emarsys.mobileengage.action.models.PresentableCustomEventActionModel
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class PushActionFactoryTests {

    private lateinit var pushActionFactory: PushActionFactory
    private lateinit var mockEventActionFactory: ActionFactoryApi<ActionModel>
    private lateinit var mockPushToInAppHandler: PushToInAppHandlerApi

    @BeforeTest
    fun setup() {
        mockEventActionFactory = mock()
        mockPushToInAppHandler = mock()
        pushActionFactory = PushActionFactory(mockPushToInAppHandler, mockEventActionFactory)
    }

    @Test
    fun create_shouldReturn_pushToInAppAction() = runTest {
        val testActionModel = InternalPushToInappActionModel("campaignId", "url")

        val result = pushActionFactory.create(testActionModel)

        result.shouldBeTypeOf<PushToInappAction>()
        verifySuspend {
            repeat(0) {
                mockEventActionFactory.create(any())
            }
        }
    }

    @Test
    fun create_should_callCreate_on_eventActionFactory() = runTest {
        val testActionModel = PresentableCustomEventActionModel("eventId", "title", "eventName")
        val expectedAction = CustomEventAction(testActionModel, mock())
        everySuspend { mockEventActionFactory.create(testActionModel) } returns expectedAction

        val result = pushActionFactory.create(testActionModel)

        result shouldBe expectedAction
    }
}