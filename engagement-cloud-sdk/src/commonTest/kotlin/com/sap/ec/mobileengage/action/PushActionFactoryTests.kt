package com.sap.ec.mobileengage.action

import com.sap.ec.core.actions.launchapplication.LaunchApplicationHandlerApi
import com.sap.ec.core.actions.pushtoinapp.PushToInAppHandlerApi
import com.sap.ec.mobileengage.action.actions.CustomEventAction
import com.sap.ec.mobileengage.action.actions.LaunchApplicationAction
import com.sap.ec.mobileengage.action.actions.PushToInappAction
import com.sap.ec.mobileengage.action.models.BasicLaunchApplicationActionModel
import com.sap.ec.mobileengage.action.models.BasicPushToInAppActionModel
import com.sap.ec.mobileengage.action.models.PresentableCustomEventActionModel
import com.sap.ec.mobileengage.action.models.PresentablePushToInAppActionModel
import com.sap.ec.mobileengage.inapp.networking.models.PushToInAppPayload
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class PushActionFactoryTests {
    private companion object {
        const val URL = "url"
        const val ID = "testId"
        const val TITLE = "testTitle"
        const val REPORTING = """{"reportingKey":"reportingValue"}"""
    }

    private lateinit var pushActionFactory: PushActionFactory
    private lateinit var mockEventActionFactory: EventActionFactoryApi
    private lateinit var mockPushToInAppHandler: PushToInAppHandlerApi
    private lateinit var mockLaunchApplicationHandler: LaunchApplicationHandlerApi

    @BeforeTest
    fun setup() {
        mockEventActionFactory = mock()
        mockPushToInAppHandler = mock()
        mockLaunchApplicationHandler = mock()
        pushActionFactory = PushActionFactory(
            mockPushToInAppHandler,
            mockEventActionFactory,
            mockLaunchApplicationHandler
        )
    }

    @Test
    fun create_shouldReturn_pushToInAppAction() = runTest {
        val testActionModel = PresentablePushToInAppActionModel(
            ID,
            REPORTING,
            TITLE,
            PushToInAppPayload(URL)
        )

        val result = pushActionFactory.create(testActionModel)

        result.shouldBeTypeOf<PushToInappAction>()
        verifySuspend(VerifyMode.exactly(0)) {
            mockEventActionFactory.create(any())
        }
    }

    @Test
    fun create_shouldReturn_pushToInAppAction_fromBasicPushToInAppActionModel() = runTest {
        val testBasicActionModel =
            BasicPushToInAppActionModel(REPORTING, PushToInAppPayload(URL))

        val result = pushActionFactory.create(testBasicActionModel)

        result.shouldBeTypeOf<PushToInappAction>()
        verifySuspend(VerifyMode.exactly(0)) {
            mockEventActionFactory.create(any())
        }
    }

    @Test
    fun create_shouldReturn_LaunchApplicationAction_fromLaunchApplicationActionModel() = runTest {
        val testBasicActionModel = BasicLaunchApplicationActionModel

        val result = pushActionFactory.create(testBasicActionModel)

        result.shouldBeTypeOf<LaunchApplicationAction>()
        verifySuspend(VerifyMode.exactly(0)) {
            mockEventActionFactory.create(any())
        }
    }

    @Test
    fun create_should_callCreate_on_eventActionFactory() = runTest {
        val testActionModel = PresentableCustomEventActionModel(
            reporting = REPORTING,
            title = "title",
            name = "eventName"
        )
        val expectedAction = CustomEventAction(testActionModel, mock())
        everySuspend { mockEventActionFactory.create(testActionModel) } returns expectedAction

        val result = pushActionFactory.create(testActionModel)

        result shouldBe expectedAction
    }
}